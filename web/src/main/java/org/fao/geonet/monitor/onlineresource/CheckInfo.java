package org.fao.geonet.monitor.onlineresource;

public class CheckInfo {

    private CheckResult checkResult;
    private long timestamp;

    public CheckInfo(OnlineResourceCheckerInterface onlineResourceChecker) {
        this.checkResult = onlineResourceChecker.check();
        this.timestamp = System.currentTimeMillis() / 1000l;
    }

    public CheckResult getCheckResult() {
        return checkResult;
    }

    public void setCheckResult(CheckResult checkResult) {
        this.checkResult = checkResult;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

}
