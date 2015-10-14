package org.fao.geonet.monitor.link;

import org.apache.log4j.Logger;
import java.util.ArrayList;
import java.util.List;

public class LinkInfo {
    private static Logger logger = Logger.getLogger(LinkInfo.class);

    private List<CheckInfo> checkInfoList;

    private final LinkCheckerInterface linkChecker;

    private LinkMonitorService.Status status = LinkMonitorService.Status.UNKNOWN;

    public LinkInfo(LinkCheckerInterface linkChecker) {
        checkInfoList = new ArrayList<CheckInfo>();
        this.linkChecker = linkChecker;
    }

    public int getCheckCount() {
        return checkInfoList.size();
    }

    private boolean isFresh() {
        long now = System.currentTimeMillis() / 1000l;
        long lastCheck = checkInfoList.get(getCheckCount() - 1).timestamp;
        if (now - lastCheck > LinkMonitorService.freshness) {
            logger.debug(String.format("Last test is too old, ('%s' seconds ago)", now - lastCheck));
            return false;
        }

        return true;
    }

    private boolean noChecksPerformed() {
        return getCheckCount() == 0;
    }

    private boolean failureRateAcceptable() {
        double checkFailureRate = checkFailureCount() / (double) LinkMonitorService.maxChecks;

        return checkFailureRate <= LinkMonitorService.maxFailureRate;
    }

    private int checkFailureCount() {
        int failedCount = 0;

        for (final CheckInfo checkInfo : checkInfoList) {
            if (!checkInfo.status) {
                failedCount++;
            }
        }

        return failedCount;
    }

    public LinkMonitorService.Status evaluateStatus() {
        if (!isFresh()) {
            return LinkMonitorService.Status.UNKNOWN;
        }

        if (noChecksPerformed()) {
            return LinkMonitorService.Status.UNKNOWN;
        }

        if (failureRateAcceptable()) {
            return LinkMonitorService.Status.WORKING;
        } else {
            return LinkMonitorService.Status.FAILED;
        }
    }

    public LinkMonitorService.Status getStatus() {
        return status;
    }

    private void truncateCheckList() {
        // Leave just the last maxChecks checks
        while (getCheckCount() > LinkMonitorService.maxChecks) {
            checkInfoList.remove(0);
        }
    }

    public void check() {
        checkInfoList.add(new CheckInfo(linkChecker));
        truncateCheckList();
        status = evaluateStatus();
    }

    @Override
    public String toString() {
        return linkChecker.toString();
    }
}
