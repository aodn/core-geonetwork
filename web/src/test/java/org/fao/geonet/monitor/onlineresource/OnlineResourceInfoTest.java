package org.fao.geonet.monitor.onlineresource;

import org.fao.geonet.test.TestCase;
import org.jdom.Element;

import java.util.List;

import static org.fao.geonet.monitor.onlineresource.OnlineResourceMonitorService.Status.*;

public class OnlineResourceInfoTest extends TestCase {
    public static CheckResult testResult = new CheckResult(true, null);

    public class OnlineResourceCheckerMock implements OnlineResourceCheckerInterface {
        public OnlineResourceCheckerMock() {}

        @Override
        public void setOnlineResource(String uuid, final Element onlineResource) {}

        @Override
        public CheckResult check() {
            return testResult;
        }

        @Override
        public String toString() {
            return "";
        }

        @Override
        public boolean canHandle(String onlineResourceType) {
            return true;
        }
    }

    public OnlineResourceInfo onlineResourceInfo;

    public void setUp() {
        OnlineResourceMonitorService.maxChecks = 10;
        OnlineResourceMonitorService.maxFailureRate = 0.2;

        onlineResourceInfo = new OnlineResourceInfo(new OnlineResourceCheckerMock());
    }

    public void testGetStatusExample1() throws Exception {
        attemptChecks(1, 1);

        assertEquals(WORKING, onlineResourceInfo.getStatus());
    }

    public void testGetStatusExample2() throws Exception {
        attemptChecks(10000, 1);

        assertEquals(WORKING, onlineResourceInfo.getStatus());
    }

    public void testGetStatusExample3() throws Exception {
        attemptChecks(10000, 3);

        assertEquals(FAILED, onlineResourceInfo.getStatus());
    }

    public void testGetStatusExample4() throws Exception {
        OnlineResourceMonitorService.maxChecks = 100;
        OnlineResourceMonitorService.maxFailureRate = 0.02;

        attemptChecks(1, 1);

        assertEquals(WORKING, onlineResourceInfo.getStatus());
    }

    public void testGetStatusExample5() throws Exception {
        OnlineResourceMonitorService.maxChecks = 100;
        OnlineResourceMonitorService.maxFailureRate = 0.02;

        attemptChecks(3, 3);

        assertEquals(FAILED, onlineResourceInfo.getStatus());

        // Check that error messages are being passed in the CheckResult object for
        // failures
        List<CheckInfo> checkInfoList = onlineResourceInfo.getCheckInfoList();
        for(CheckInfo check : checkInfoList) {
            if(!check.getCheckResult().isSuccessful()) {
                assertTrue(check.getCheckResult().getResultReason() != null);
            }
        }
    }

    public void testGetStatusNoChecks() throws Exception {
        attemptChecks(0, 0);

        assertEquals(UNKNOWN, onlineResourceInfo.getStatus());
    }

    public void testGetCheckCount() throws Exception {
        attemptChecks(1000, 0);

        assertEquals(OnlineResourceMonitorService.maxChecks, onlineResourceInfo.getCheckCount());
    }

    private void attemptChecks(int numberOfChecks, int numberOfFailures) {
        int numberOfSuccesses = numberOfChecks - numberOfFailures;

        testResult = new CheckResult(true, null);
        for (int i = 0; i < numberOfSuccesses; i++) {
            onlineResourceInfo.check();
        }

        testResult = new CheckResult(false, "Test failure message");
        for (int i = 0; i < numberOfFailures; i++) {
            onlineResourceInfo.check();
        }
    }
}
