package org.fao.geonet.monitor.onlineresource;

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

public class OnlineResourceMonitorService implements OnlineResourceMonitorInterface {

    static Logger logger = Logger.getLogger(OnlineResourceMonitorService.class);

    private static ApplicationContext applicationContext;
    private ResourceManager resourceManager;
    private GeonetContext geonetContext;

    public enum Status {
        FAILED,
        WORKING,
        UNKNOWN
    }

    public static final String ONLINE_RESOURCE_MONITOR_SERVICE_REINDEXINTERVALSECONDS = "OnlineResourceMonitorServiceReindexIntervalSeconds";
    private long reindexInterval;

    public static final String ONLINE_RESOURCE_MONITOR_SERVICE_MAXFAILURERATE = "OnlineResourceMonitorServiceMaxFailureRate";
    public static double maxFailureRate;

    public static final String ONLINE_RESOURCE_MONITOR_SERVICE_MAXCHECKS = "OnlineResourceMonitorServiceMaxChecks";
    public static int maxChecks;

    public static final String ONLINE_RESOURCE_MONITOR_SERVICE_TIMEOUT = "OnlineResourceMonitorServiceTimeout";
    public static int timeout;

    public static final String ONLINE_RESOURCE_MONITOR_SERVICE_FRESHNESS = "OnlineResourceMonitorServiceFreshness";
    public static int freshness;

    public static final String ONLINE_RESOURCE_MONITOR_SERVICE_UNKNOWNASWORKING = "OnlineResourceMonitorServiceUnknownAsWorking";
    private boolean unknownAsWorking;

    // Milliseconds between running checks on every record. This is here to
    // prevent undesired hammering of servers
    public static final String ONLINE_RESOURCE_MONITOR_SERVICE_BETWEENCHECKSINTERVALMS = "OnlineResourceMonitorServiceBetweenChecksIntervalMs";
    private static int betweenChecksIntervalMs;

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

        this.reindexInterval = Integer.parseInt(serviceConfig.getValue(ONLINE_RESOURCE_MONITOR_SERVICE_REINDEXINTERVALSECONDS, "600"));
        this.maxFailureRate = Double.parseDouble(serviceConfig.getValue(ONLINE_RESOURCE_MONITOR_SERVICE_MAXFAILURERATE, "0.1"));
        this.maxChecks = Integer.parseInt(serviceConfig.getValue(ONLINE_RESOURCE_MONITOR_SERVICE_MAXCHECKS, "10"));
        this.timeout = Integer.parseInt(serviceConfig.getValue(ONLINE_RESOURCE_MONITOR_SERVICE_TIMEOUT, "15"));
        this.freshness = Integer.parseInt(serviceConfig.getValue(ONLINE_RESOURCE_MONITOR_SERVICE_FRESHNESS, "3600"));
        this.unknownAsWorking = Boolean.parseBoolean(serviceConfig.getValue(ONLINE_RESOURCE_MONITOR_SERVICE_UNKNOWNASWORKING, "true"));
        this.betweenChecksIntervalMs = Integer.parseInt(serviceConfig.getValue(ONLINE_RESOURCE_MONITOR_SERVICE_BETWEENCHECKSINTERVALMS, "100"));

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
                logger.info(String.format("You might want to tune '%s'", Geonet.Config.ONLINE_RESOURCE_MONITOR_FIXEDDELAYSECONDS));
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

    private Map<String, MetadataRecordInfo> getAllRecords() {
        try {
            return getAllRecordsLucene();
        } catch (Exception e) {
            logger.info(e);
        }
        return new HashMap<String, MetadataRecordInfo>();
    }

    private long stringDateToLong(String date) {
        DateTime dateTime = new DateTime(date);
        return dateTime.getMillis();
    }

    private Map<String, MetadataRecordInfo> getAllRecordsLucene() throws Exception {
        Map<String, MetadataRecordInfo> records = new HashMap<String, MetadataRecordInfo>();

        IndexAndTaxonomy indexAndTaxonomy = geonetContext.getSearchmanager().getNewIndexReader(null);
        try {
            GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

            for (int i = 0; i < reader.maxDoc(); i++) {
                DocumentStoredFieldVisitor idChangeDateSelector = new DocumentStoredFieldVisitor("_uuid", "_changeDate", "title", "altTitle");
                reader.document(i, idChangeDateSelector);
                org.apache.lucene.document.Document doc = idChangeDateSelector.getDocument();
                String uuid = doc.get("_uuid");
                String title = doc.get("title");

                if (uuid == null) {
                    logger.error("Document with no _uuid field skipped! Document is " + doc);
                    continue;
                } else {
                    logger.info(String.format("Link Monitor Service adding title=%s uuid=%s", title, uuid));
                    long lastUpdated = stringDateToLong(doc.get("_changeDate"));
                    records.put(uuid, new MetadataRecordInfo(this, uuid, title, lastUpdated));
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

    private void reindex(Map<String, MetadataRecordInfo> records) {
        logger.info("Link Monitor Service is reindexing...");

        for (final String uuid : records.keySet()) {

            MetadataRecordInfo metadataRecordInfo = records.get(uuid);
            Long updated = metadataRecordInfo.getLastUpdated();
            String title = metadataRecordInfo.getTitle();

            if (recordMap.containsKey(uuid)) {
                if (recordMap.get(uuid).getLastUpdated() < updated) {
                    logger.debug(String.format("Updating metadata record title=%s uuid=%s ", title, uuid));
                    metadataRecordInfo.setLastUpdated(updated);
                    recordMap.put(uuid, metadataRecordInfo);
                }
            } else {
                // New recordMap
                logger.debug(String.format("New metadata record title=%s uuid=%s ", title, uuid));
                recordMap.put(uuid, new MetadataRecordInfo(this, uuid, title, updated));
            }
        }

        for (Map.Entry<String, MetadataRecordInfo> record : recordMap.entrySet()) {
            String title = records.get(record.getKey()).getTitle();
            if (! records.containsKey(record.getKey())) {
                // Record was deleted
                logger.info(String.format("Deleting metadata record title=%s uuid=%s ", title, record.getKey()));
                recordMap.remove(record.getKey());
            }
        }
        reindexTimestamp = System.currentTimeMillis() / 1000l;
    }

    public void check() {
        if (needReindex()) {
            reindex(getAllRecords());
        }

        for (Map.Entry<String, MetadataRecordInfo> record : recordMap.entrySet()) {

            MetadataRecordInfo rec = recordMap.get(record.getKey());
            logger.info(String.format("Checking record title=%s uuid=%s ", rec.getTitle(), record.getKey()));
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
