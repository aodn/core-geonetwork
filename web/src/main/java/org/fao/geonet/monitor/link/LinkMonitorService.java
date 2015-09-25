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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LinkMonitorService implements LinkMonitorInterface {

    static Logger logger = Logger.getLogger(LinkMonitorService.class);

    private ResourceManager resourceManager;
    private GeonetContext geonetContext;

    public enum Status {
        FAILED,
        WORKING,
        UNKNOWN
    }

    public static final String LINK_MONITOR_SERVICE_REINDEXINTERVALSECONDS = "LinkMonitorServiceReindexIntervalSeconds";
    private long reindexInterval;

    public static final String LINK_MONITOR_SERVICE_PERCENTWORKINGTHRESHOLD = "LinkMonitorServicePercentWorkingThreshold";
    public static int percentWorkingThreshold = 90;

    public static final String LINK_MONITOR_SERVICE_MAXCHECKS = "LinkMonitorServiceMaxChecks";
    public static int maxChecks = 10;

    public static final String LINK_MONITOR_SERVICE_TIMEOUT = "LinkMonitorServiceTimeout";
    public static int timeout;

    public static final String LINK_MONITOR_SERVICE_FRESHNESS = "LinkMonitorServiceFreshness";
    public static int freshness;

    public static final String LINK_MONITOR_SERVICE_UNKNOWNASWORKING = "LinkMonitorServiceUnknownAsWorking";
    private boolean unknownAsWorking;

    // Milliseconds between running checks on every record. This is here to
    // prevent undesired hammering of servers
    public static final String LINK_MONITOR_SERVICE_BETWEENCHECKSINTERVALMS = "LinkMonitorServiceBetweenChecksIntervalMs";
    private static int betweenChecksIntervalMs;

    private final Map<String, MetadataRecordInfo> recordMap;

    private long reindexTimestamp = -1;

    // Prevent ourselves from being triggered while there is an ongoing check
    private static long UNDEFINED_THREAD_ID;
    private long runningThreadId = UNDEFINED_THREAD_ID;

    public LinkMonitorService() {
        this.recordMap = new HashMap<String, MetadataRecordInfo>();
    }

    @Override
    public void init(ResourceManager resourceManager, GeonetContext geonetContext, ServiceConfig serviceConfig) {
        this.resourceManager = resourceManager;
        this.geonetContext = geonetContext;
        this.reindexInterval = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_REINDEXINTERVALSECONDS, "1800"));
        this.percentWorkingThreshold = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_PERCENTWORKINGTHRESHOLD, "90"));
        this.maxChecks = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_MAXCHECKS, "10"));
        this.timeout = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_TIMEOUT, "15"));
        this.freshness = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_FRESHNESS, "3600"));
        this.unknownAsWorking = Boolean.parseBoolean(serviceConfig.getValue(LINK_MONITOR_SERVICE_UNKNOWNASWORKING, "true"));
        this.betweenChecksIntervalMs = Integer.parseInt(serviceConfig.getValue(LINK_MONITOR_SERVICE_BETWEENCHECKSINTERVALMS, "100"));
    }

    private void setStopRunning() {
        setRunning(false);
    }

    private boolean setStartRunning() {
        return setRunning(true);
    }

    private synchronized boolean setRunning(boolean running) {
        long currentThreadId = Thread.currentThread().getId();

        if (running) {
            if (runningThreadId == UNDEFINED_THREAD_ID) {
                runningThreadId = currentThreadId;
                return true;
            } else {
                return false;
            }
        }

        if (!running) {
            if (runningThreadId == currentThreadId) {
                runningThreadId = UNDEFINED_THREAD_ID;
                return true;
            } else {
                return false;
            }
        }

        return false;
    }

    @Override
    public void run() {
        try {
            if (setStartRunning()) {
                check();
            } else {
                logger.info("Check is already in progress, skipping...");
                logger.info(String.format("You might want to tune '%s'", Geonet.Config.LINK_MONITOR_FIXEDDELAYSECONDS));
            }
        } catch(Throwable e) {
            logger.error("Link Monitor error: " + e.getMessage() + " This error is ignored.");
            logger.info(e);
        } finally {
            setStopRunning();
        }
    }

    private ArrayList<String> getAllRecords() {
        try {
            return getAllRecordsLucene();
        } catch (Exception e) {
            logger.info(e);
        }
        return new ArrayList<String>();
    }

    private ArrayList<String> getAllRecordsLucene() throws Exception {
        ArrayList<String> records = new ArrayList<String>();

        IndexAndTaxonomy indexAndTaxonomy = geonetContext.getSearchmanager().getNewIndexReader(null);
        try {
            GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

            int capacity = (int)(reader.maxDoc() / 0.75) + 1;
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
                    records.add(uuid);
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

    private void reindex(ArrayList<String> records) {
        logger.info("Link Monitor Service is reindexing...");

        for (final String uuid : records) {
            if (recordMap.get(uuid) == null) {
                // New record
                recordMap.put(uuid, new MetadataRecordInfo(this, uuid));
            }
        }

        for (Map.Entry<String, MetadataRecordInfo> record : recordMap.entrySet()) {
            if (! records.contains(record.getKey())) {
                // Record was deleted
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
