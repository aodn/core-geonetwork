package org.fao.geonet.monitor.link;

import org.fao.geonet.test.TestCase;
import org.jdom.Element;

import static org.fao.geonet.monitor.link.LinkMonitorService.Status.*;

public class LinkInfoTest extends TestCase {
    public static boolean testResult = true;

    public class LinkCheckerMock implements LinkCheckerInterface {
        public LinkCheckerMock() {}

        @Override
        public void setOnlineResource(final Element onlineResource) {}

        @Override
        public boolean check() {
            return testResult;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public boolean canHandle(String linkType) {
            return true;
        }
    }

    public LinkInfo linkInfo;

    public void setUp() {
        linkInfo = new LinkInfo(new LinkCheckerMock());
    }

    public void testGetStatusExample1() throws Exception {
        LinkMonitorService.maxChecks = 10;
        LinkMonitorService.maxFailureRate = 0.2;

        attemptChecks(1, 1);

        assertEquals(WORKING, linkInfo.getStatus());
    }

    public void testGetStatusExample2() throws Exception {
        LinkMonitorService.maxChecks = 10;
        LinkMonitorService.maxFailureRate = 0.2;

        attemptChecks(10000, 1);

        assertEquals(WORKING, linkInfo.getStatus());
    }

    public void testGetStatusExample3() throws Exception {
        LinkMonitorService.maxChecks = 10;
        LinkMonitorService.maxFailureRate = 0.2;

        attemptChecks(10000, 3);

        assertEquals(FAILED, linkInfo.getStatus());
    }

    public void testGetStatusExample4() throws Exception {
        LinkMonitorService.maxChecks = 100;
        LinkMonitorService.maxFailureRate = 0.02;

        attemptChecks(1, 1);

        assertEquals(WORKING, linkInfo.getStatus());
    }

    public void testGetStatusExample5() throws Exception {
        LinkMonitorService.maxChecks = 100;
        LinkMonitorService.maxFailureRate = 0.02;

        attemptChecks(3, 3);

        assertEquals(FAILED, linkInfo.getStatus());
    }

    public void testGetStatus() throws Exception {
        LinkMonitorService.maxChecks = 3;
        LinkMonitorService.maxFailureRate = 0.5;

        LinkInfo linkInfo = new LinkInfo(new LinkCheckerMock());
        testResult = true;

        // No checks done - status should be UNKNOWN
        assertEquals(LinkMonitorService.Status.UNKNOWN, linkInfo.getStatus());

        // One check out of 3 done - status should be WORKING
        linkInfo.check();
        assertEquals(LinkMonitorService.Status.WORKING, linkInfo.getStatus());

        // 3 checks done, should be WORKING
        linkInfo.check();
        linkInfo.check();
        assertEquals(LinkMonitorService.Status.WORKING, linkInfo.getStatus());

        // 1 check is now failing (out of 3), but we need only 50%, so still working
        testResult = false;
        linkInfo.check();
        assertEquals(LinkMonitorService.Status.WORKING, linkInfo.getStatus());

        // 2 checks out of 3 are failing, so it should be failing
        linkInfo.check();
        assertEquals(LinkMonitorService.Status.FAILED, linkInfo.getStatus());
    }

    public void testGetStatusAllFailed() throws Exception {
        LinkMonitorService.maxChecks = 10;
        LinkMonitorService.maxFailureRate = 0.9;

        LinkInfo linkInfo = new LinkInfo(new LinkCheckerMock());
        testResult = false;

        // Not enough checks done, but all have failed
        linkInfo.check();
        linkInfo.check();
        assertEquals(LinkMonitorService.Status.FAILED, linkInfo.getStatus());
    }

    public void testCheck() throws Exception {
        // Verify we save only 3 checks back
        LinkMonitorService.maxChecks = 3;
        LinkInfo linkInfo = new LinkInfo(new LinkCheckerMock());
        for (int i = 0; i < 10; i++) {
            linkInfo.check();
        }

        assertEquals(LinkMonitorService.maxChecks, linkInfo.getCheckCount());
    }

    private void attemptChecks(int numberOfChecks, int numberOfFailures) {
        int numberOfSuccesses = numberOfChecks - numberOfFailures;

        testResult = true;
        for (int i = 0; i < numberOfSuccesses; i++) {
            linkInfo.check();
        }

        testResult = false;
        for (int i = 0; i < numberOfFailures; i++) {
            linkInfo.check();
        }
    }
}
