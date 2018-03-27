package org.fao.geonet.monitor.onlineresource;

public class CheckResult {

    private CheckResultEnum result;

    private String resultReason;

    public CheckResult(CheckResultEnum result, String resultReason) {
        this.result = result;
        this.resultReason = resultReason;
    }

    public CheckResultEnum getResult() {
        return result;
    }

    public String getResultReason() {
        return resultReason;
    }
}
