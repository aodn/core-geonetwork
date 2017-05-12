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
        public String getLastErrorMsg() { return ""; }

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
        LinkMonitorService.maxChecks = 10;
        LinkMonitorService.maxFailureRate = 0.2;

        linkInfo = new LinkInfo(new LinkCheckerMock());
    }

    public void testGetStatusExample1() throws Exception {
        attemptChecks(1, 1);

        assertEquals(WORKING, linkInfo.getStatus());
    }

    public void testGetStatusExample2() throws Exception {
        attemptChecks(10000, 1);

        assertEquals(WORKING, linkInfo.getStatus());
    }

    public void testGetStatusExample3() throws Exception {
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

    public void testGetStatusNoChecks() throws Exception {
        attemptChecks(0, 0);

        assertEquals(UNKNOWN, linkInfo.getStatus());
    }

    public void testGetCheckCount() throws Exception {
        attemptChecks(1000, 0);

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
