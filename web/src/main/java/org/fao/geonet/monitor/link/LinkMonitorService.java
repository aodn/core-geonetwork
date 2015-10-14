package org.fao.geonet.monitor.link;

import jeeves.resources.dbms.Dbms;
import jeeves.server.ServiceConfig;
import jeeves.server.resources.ResourceManager;
import org.apache.log4j.Logger;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;
import org.jdom.Element;
import org.joda.time.DateTime;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

public class LinkMonitorService implements LinkMonitorInterface {

    static Logger logger = Logger.getLogger(LinkMonitorService.class);

    private static ApplicationContext applicationContext;
    private ResourceManager resourceManager;
    private GeonetContext geonetContext;

    public enum Status {
        FAILED,
        WORKING,
        UNKNOWN
    }

    public static final String LINK_MONITOR_SERVICE_REINDEXINTERVALSECONDS = "LinkMonitorServiceReindexIntervalSeconds";
    private long reindexInterval = 1800;

    public static final String LINK_MONITOR_SERVICE_MAXFAILURERATE = "LinkMonitorServiceMaxFailureRate";
    public static double maxFailureRate = 0.1;

    public static final String LINK_MONITOR_SERVICE_MAXCHECKS = "LinkMonitorServiceMaxChecks";
    public static int maxChecks = 10;

    public static final String LINK_MONITOR_SERVICE_TIMEOUT = "LinkMonitorServiceTimeout";
    public static int timeout = 15;

    public static final String LINK_MONITOR_SERVICE_FRESHNESS = "LinkMonitorServiceFreshness";
    public static int freshness = 3600;

    public static final String LINK_MONITOR_SERVICE_UNKNOWNASWORKING = "LinkMonitorServiceUnknownAsWorking";
    private boolean unknownAsWorking = true;

    // Milliseconds between running checks on every record. This is here to
    // prevent undesired hammering of servers
    public static final String LINK_MONITOR_SERVICE_BETWEENCHECKSINTERVALMS = "LinkMonitorServiceBetweenChecksIntervalMs";
    private static int betweenChecksIntervalMs = 2000;

    private final Map<String, MetadataRecordInfo> recordMap = new HashMap<String, MetadataRecordInfo>();

    private long reindexTimestamp = -1;

    // Prevent ourselves from being triggered while there is an ongoing check
    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void init(
        ApplicationContext applicationContext,
        ResourceManager resourceManager,
        GeonetContext geonetContext,
        ServiceConfig serviceConfig) {
        this.applicationContext = applicationContext;
        this.resourceManager = resourceManager;
        this.geonetContext = geonetContext;

        this.reindexInterval = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_REINDEXINTERVALSECONDS, "1800"));
        this.maxFailureRate = Double.parseDouble(serviceConfig.getValue(LINK_MONITOR_SERVICE_MAXFAILURERATE, "0.1"));
        this.maxChecks = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_MAXCHECKS, "10"));
        this.timeout = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_TIMEOUT, "15"));
        this.freshness = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_FRESHNESS, "3600"));
        this.unknownAsWorking = Boolean.parseBoolean(serviceConfig.getValue(LINK_MONITOR_SERVICE_UNKNOWNASWORKING, "true"));
        this.betweenChecksIntervalMs = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_BETWEENCHECKSINTERVALMS, "100"));

        if (maxFailureRate > 1) maxFailureRate = 1;
        if (maxFailureRate < 0) maxFailureRate = 0;
    }

    @Override
    public void run() {
        try {
            if (lock.tryLock()) {
                check();
            } else {
                logger.info("Check is already in progress, skipping...");
                logger.info(String.format("You might want to tune '%s'", Geonet.Config.LINK_MONITOR_FIXEDDELAYSECONDS));
            }
        } catch(Throwable e) {
            logger.error("Link Monitor error: " + e.getMessage() + " This error is ignored.");
            logger.info(e);
        } finally {
            lock.unlock();
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private Map<String, Long> getAllRecords() {
        try {
            return getAllRecordsLucene();
        } catch (Exception e) {
            logger.info(e);
        }
        return new HashMap<String, Long>();
    }

    private long stringDateToLong(String date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.getMillis();
    }

    private Map<String, Long> getAllRecordsLucene() throws Exception {
        Map<String, Long> records = new HashMap<String, Long>();

        IndexAndTaxonomy indexAndTaxonomy = geonetContext.getSearchmanager().getNewIndexReader(null);
        try {
            GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

            for (int i = 0; i < reader.maxDoc(); i++) {
                DocumentStoredFieldVisitor idChangeDateSelector = new DocumentStoredFieldVisitor("_uuid", "_changeDate");
                reader.document(i, idChangeDateSelector);
                org.apache.lucene.document.Document doc = idChangeDateSelector.getDocument();
                String uuid = doc.get("_uuid");

                if (uuid == null) {
                    logger.error("Document with no _uuid field skipped! Document is " + doc);
                    continue;
                } else {
                    logger.debug(String.format("Link Monitor Service adding uuid '%s'", uuid));
                    long lastUpdated = stringDateToLong(doc.get("_changeDate"));
                    records.put(uuid, lastUpdated);
                }
            }
        } catch (Exception e) {
            logger.info(e);
        } finally {
            geonetContext.getSearchmanager().releaseIndexReader(indexAndTaxonomy);
        }

        return records;
    }

    public Element getDocumentForUuid(String uuid) {
        try {
            Dbms dbms = (Dbms) resourceManager.open(Geonet.Res.MAIN_DB);
            String id = geonetContext.getDataManager().getMetadataId(dbms, uuid);
            return geonetContext.getDataManager().getMetadataIgnorePermissions(dbms, id);
        } catch (Exception e) {
            logger.info(e);
        }
        return null;
    }

    private boolean needReindex() {
        long now = System.currentTimeMillis() / 1000l;
        return reindexTimestamp < 0 || now - reindexTimestamp >= reindexInterval;
    }

    private void reindex(Map<String, Long> records) {
        logger.info("Link Monitor Service is reindexing...");

        for (final String uuid : records.keySet()) {
            long lastUpdated = records.get(uuid);

            if (recordMap.containsKey(uuid)) {
                if (recordMap.get(uuid).getLastUpdated() < lastUpdated) {
                    logger.info(String.format("Metadata record '%s' was recently changed, updating it...", uuid));
                    recordMap.put(uuid, new MetadataRecordInfo(this, uuid, lastUpdated));
                }
            } else {
                // New record
                logger.debug(String.format("New metadata record '%s'", uuid));
                recordMap.put(uuid, new MetadataRecordInfo(this, uuid, lastUpdated));
            }
        }

        for (Map.Entry<String, MetadataRecordInfo> record : recordMap.entrySet()) {
            if (! records.containsKey(record.getKey())) {
                // Record was deleted
                logger.debug(String.format("Record '%s' was deleted", record.getKey()));
                recordMap.remove(record.getKey());
            }
        }

        reindexTimestamp = System.currentTimeMillis() / 1000l;
    }

    public void check() {
        if (needReindex())
            reindex(getAllRecords());

        for (Map.Entry<String, MetadataRecordInfo> record : recordMap.entrySet()) {
            logger.debug(String.format("Checking record '%s'", record.getKey()));
            record.getValue().check();
            try {
                Thread.sleep(betweenChecksIntervalMs);
            } catch (InterruptedException e) {
                logger.info(e);
            }
        }
    }

    @Override
    public boolean isHealthy(String uuid) {
        MetadataRecordInfo record = recordMap.get(uuid);

        if (record != null) {
            return record.isHealthy(unknownAsWorking);
        } else {
            return true;
        }
    }
}
