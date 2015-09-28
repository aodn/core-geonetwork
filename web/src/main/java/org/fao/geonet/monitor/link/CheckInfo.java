package org.fao.geonet.monitor.link;

public class CheckInfo {
    public boolean status;
    public long timestamp;

    public CheckInfo(LinkCheckerInterface linkChecker) {
        this.status = linkChecker.check();
        this.timestamp = System.currentTimeMillis() / 1000l;
    }
}
