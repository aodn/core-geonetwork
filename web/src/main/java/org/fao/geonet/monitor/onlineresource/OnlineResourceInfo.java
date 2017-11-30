package org.fao.geonet.monitor.onlineresource;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class OnlineResourceInfo {
    private static Logger logger = Logger.getLogger(OnlineResourceInfo.class);

    public List<CheckInfo> getCheckInfoList() {
        return checkInfoList;
    }

    private List<CheckInfo> checkInfoList;

    private final OnlineResourceCheckerInterface onlineResourceChecker;

    private OnlineResourceMonitorService.Status status = OnlineResourceMonitorService.Status.UNKNOWN;

    public OnlineResourceInfo(OnlineResourceCheckerInterface onlineResourceChecker) {
        checkInfoList = new ArrayList<CheckInfo>();
        this.onlineResourceChecker = onlineResourceChecker;
    }

    public int getCheckCount() {
        return checkInfoList.size();
    }

    public int getFailureCount() {
        int failedCount = 0;

        for (final CheckInfo checkInfo : checkInfoList) {
            if (!checkInfo.getCheckResult().isSuccessful()) {
                failedCount++;
            }
        }

        return failedCount;
    }

    private boolean isFresh() {
        long now = System.currentTimeMillis() / 1000l;
        long lastCheck = checkInfoList.get(getCheckCount() - 1).getTimestamp();
        if (now - lastCheck > OnlineResourceMonitorService.freshness) {
            logger.debug(String.format("Last test is too old, ('%s' seconds ago)", now - lastCheck));
            return false;
        }

        return true;
    }

    private boolean noChecksPerformed() {
        return getCheckCount() == 0;
    }

    private boolean failureRateAcceptable() {
        double checkFailureRate = getFailureCount() / (double) OnlineResourceMonitorService.maxChecks;

        return checkFailureRate <= OnlineResourceMonitorService.maxFailureRate;
    }

    public OnlineResourceMonitorService.Status evaluateStatus() {
        if (!isFresh()) {
            return OnlineResourceMonitorService.Status.UNKNOWN;
        }

        if (noChecksPerformed()) {
            return OnlineResourceMonitorService.Status.UNKNOWN;
        }

        if (failureRateAcceptable()) {
            return OnlineResourceMonitorService.Status.WORKING;
        } else {
            return OnlineResourceMonitorService.Status.FAILED;
        }
    }

    public OnlineResourceMonitorService.Status getStatus() {
        return status;
    }

    private void truncateCheckList() {
        // Leave just the last maxChecks checks
        while (getCheckCount() > OnlineResourceMonitorService.maxChecks) {
            checkInfoList.remove(0);
        }
    }

    public void check() {
        checkInfoList.add(new CheckInfo(onlineResourceChecker));
        truncateCheckList();
        status = evaluateStatus();
    }

    @Override
    public String toString() {
        return onlineResourceChecker.toString();
    }
}
