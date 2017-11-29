package org.fao.geonet.monitor.onlineresource;

public class CheckResult {

    private boolean isSuccessful;

    private String resultReason;

    public CheckResult(boolean isSuccessful, String resultReason) {
        this.isSuccessful = isSuccessful;
        this.resultReason = resultReason;
    }

    public boolean isSuccessful() {
        return isSuccessful;
    }

    public void setIsSuccessful(boolean isSuccessful) {
        this.isSuccessful = isSuccessful;
    }

    public String getResultReason() {
        return resultReason;
    }

    public void setResultReason(String resultReason) {
        this.resultReason = resultReason;
    }
}
