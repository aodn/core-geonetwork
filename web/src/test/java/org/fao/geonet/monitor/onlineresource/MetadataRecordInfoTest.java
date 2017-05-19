package org.fao.geonet.monitor.onlineresource;

import org.fao.geonet.test.TestCase;

import java.util.ArrayList;
import java.util.List;

public class MetadataRecordInfoTest extends TestCase {
    public void testIsHealthy() throws Exception {
        assertEquals(true, MetadataRecordInfo.isHealthy(true, OnlineResourceMonitorService.Status.WORKING));
        assertEquals(true, MetadataRecordInfo.isHealthy(false, OnlineResourceMonitorService.Status.WORKING));

        assertEquals(true, MetadataRecordInfo.isHealthy(true, OnlineResourceMonitorService.Status.UNKNOWN));
        assertEquals(false, MetadataRecordInfo.isHealthy(false, OnlineResourceMonitorService.Status.UNKNOWN));

        assertEquals(false, MetadataRecordInfo.isHealthy(true, OnlineResourceMonitorService.Status.FAILED));
        assertEquals(false, MetadataRecordInfo.isHealthy(false, OnlineResourceMonitorService.Status.FAILED));
    }

    public void testGetStatus() throws Exception {
        // All online resources are healthy
        List<OnlineResourceMonitorService.Status> statusList= new ArrayList<OnlineResourceMonitorService.Status>();

        statusList.clear();
        statusList.add(OnlineResourceMonitorService.Status.WORKING);
        assertEquals(OnlineResourceMonitorService.Status.WORKING, MetadataRecordInfo.evaluateStatus(statusList));

        statusList.clear();
        statusList.add(OnlineResourceMonitorService.Status.FAILED);
        assertEquals(OnlineResourceMonitorService.Status.FAILED, MetadataRecordInfo.evaluateStatus(statusList));

        statusList.clear();
        statusList.add(OnlineResourceMonitorService.Status.UNKNOWN);
        assertEquals(OnlineResourceMonitorService.Status.UNKNOWN, MetadataRecordInfo.evaluateStatus(statusList));

        statusList.clear();
        statusList.add(OnlineResourceMonitorService.Status.UNKNOWN);
        statusList.add(OnlineResourceMonitorService.Status.WORKING);
        statusList.add(OnlineResourceMonitorService.Status.UNKNOWN);
        assertEquals(OnlineResourceMonitorService.Status.UNKNOWN, MetadataRecordInfo.evaluateStatus(statusList));

        statusList.clear();
        statusList.add(OnlineResourceMonitorService.Status.WORKING);
        statusList.add(OnlineResourceMonitorService.Status.UNKNOWN);
        statusList.add(OnlineResourceMonitorService.Status.FAILED);
        assertEquals(OnlineResourceMonitorService.Status.FAILED, MetadataRecordInfo.evaluateStatus(statusList));
    }
}
