package org.fao.geonet.monitor.onlineresource;

public class CheckInfo {
    public boolean status;
    public long timestamp;

    public CheckInfo(OnlineResourceCheckerInterface onlineResourceChecker) {
        this.status = onlineResourceChecker.check();
        this.timestamp = System.currentTimeMillis() / 1000l;
    }
}
