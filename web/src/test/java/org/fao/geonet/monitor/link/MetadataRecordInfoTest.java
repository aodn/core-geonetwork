package org.fao.geonet.monitor.link;

import org.fao.geonet.test.TestCase;

import java.util.ArrayList;
import java.util.List;

public class MetadataRecordInfoTest extends TestCase {
    public void testIsHealthy() throws Exception {
        assertEquals(true, MetadataRecordInfo.isHealthy(true, LinkMonitorService.Status.WORKING));
        assertEquals(true, MetadataRecordInfo.isHealthy(false, LinkMonitorService.Status.WORKING));

        assertEquals(true, MetadataRecordInfo.isHealthy(true, LinkMonitorService.Status.UNKNOWN));
        assertEquals(false, MetadataRecordInfo.isHealthy(false, LinkMonitorService.Status.UNKNOWN));

        assertEquals(false, MetadataRecordInfo.isHealthy(true, LinkMonitorService.Status.FAILED));
        assertEquals(false, MetadataRecordInfo.isHealthy(false, LinkMonitorService.Status.FAILED));
    }

    public void testGetStatus() throws Exception {
        // All online resources are healthy
        List<LinkMonitorService.Status> statusList= new ArrayList<LinkMonitorService.Status>();

        statusList.clear();
        statusList.add(LinkMonitorService.Status.WORKING);
        assertEquals(LinkMonitorService.Status.WORKING, MetadataRecordInfo.getStatus(statusList));

        statusList.clear();
        statusList.add(LinkMonitorService.Status.FAILED);
        assertEquals(LinkMonitorService.Status.FAILED, MetadataRecordInfo.getStatus(statusList));

        statusList.clear();
        statusList.add(LinkMonitorService.Status.UNKNOWN);
        assertEquals(LinkMonitorService.Status.UNKNOWN, MetadataRecordInfo.getStatus(statusList));

        statusList.clear();
        statusList.add(LinkMonitorService.Status.UNKNOWN);
        statusList.add(LinkMonitorService.Status.WORKING);
        statusList.add(LinkMonitorService.Status.UNKNOWN);
        assertEquals(LinkMonitorService.Status.UNKNOWN, MetadataRecordInfo.getStatus(statusList));

        statusList.clear();
        statusList.add(LinkMonitorService.Status.WORKING);
        statusList.add(LinkMonitorService.Status.UNKNOWN);
        statusList.add(LinkMonitorService.Status.FAILED);
        assertEquals(LinkMonitorService.Status.FAILED, MetadataRecordInfo.getStatus(statusList));
    }
}