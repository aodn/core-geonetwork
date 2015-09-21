package org.fao.geonet.monitor.link;

import jeeves.interfaces.Logger;
import jeeves.server.resources.ResourceManager;
import jeeves.utils.Log;
import org.apache.lucene.document.DocumentStoredFieldVisitor;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.kernel.search.IndexAndTaxonomy;
import org.fao.geonet.kernel.search.index.GeonetworkMultiReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LinkMonitorService extends LinkMonitorInterface {

    public enum Status {
        FAILED,
        WORKING,
        UNKNOWN
    }

    public static int percentWorking = 90;

    public static int maxChecks = 10;

    public static int timeout = 15;

    public static int freshness = 60 * 60;

    private boolean unknownAsWorking = true;

    // Milliseconds between running checks on every record. This is here to
    // prevent undesired hammering of servers
    private static int betweenChecksIntervalMs = 100;

    private final Map<String, MetadataRecordInfo> recordMap;

    private long reindexTimestamp = -1;

    public LinkMonitorService() {
        this.recordMap = new HashMap<String, MetadataRecordInfo>();
    }

    @Override
    public void init(ResourceManager resourceManager, GeonetContext geonetContext, Logger logger, long reindexInterval) {
        super.init(resourceManager, geonetContext, logger, reindexInterval);
    }

    private ArrayList<String> getAllRecords() {
        try {
            return getAllRecordsLucene();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ArrayList<String> getAllRecordsLucene() throws Exception {
        ArrayList<String> records = new ArrayList<String>();

        IndexAndTaxonomy indexAndTaxonomy = getGeonetContext().getSearchmanager().getNewIndexReader(null);
        try {
            GeonetworkMultiReader reader = indexAndTaxonomy.indexReader;

            int capacity = (int)(reader.maxDoc() / 0.75) + 1;
            for (int i = 0; i < reader.maxDoc(); i++) {
                DocumentStoredFieldVisitor idChangeDateSelector = new DocumentStoredFieldVisitor("_uuid", "_changeDate");
                reader.document(i, idChangeDateSelector);
                org.apache.lucene.document.Document doc = idChangeDateSelector.getDocument();
                String uuid = doc.get("_uuid");

                if (uuid == null) {
                    Log.error(Geonet.INDEX_ENGINE, "Document with no _uuid field skipped! Document is " + doc);
                    continue;
                } else {
                    getLogger().debug(String.format("Link Monitor Service adding uuid '%s'", uuid));
                    records.add(uuid);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            getGeonetContext().getSearchmanager().releaseIndexReader(indexAndTaxonomy);
        }

        return records;
    }

    private boolean needReindex() {
        long now = System.currentTimeMillis() / 1000l;
        return reindexTimestamp < 0 || now - reindexTimestamp >= getReindexInterval();
    }

    private void reindex(ArrayList<String> records) {
        getLogger().info("Link Monitor Service is reindexing...");

        for (final String uuid : records) {
            if (recordMap.get(uuid) == null) {
                // New record
                recordMap.put(uuid, new MetadataRecordInfo(uuid));
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

    @Override
    public void check() {
        if (needReindex())
            reindex(getAllRecords());

        for (Map.Entry<String, MetadataRecordInfo> record : recordMap.entrySet()) {
            getLogger().debug(String.format("Checking record '%s'", record.getKey()));
            record.getValue().check();
            try {
                Thread.sleep(betweenChecksIntervalMs);
            } catch (InterruptedException e) {
                e.printStackTrace();
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