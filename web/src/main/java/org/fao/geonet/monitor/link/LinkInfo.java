package org.fao.geonet.monitor.link;

import org.apache.log4j.Logger;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
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

    private boolean hasSuccessCheck() {
        for (final CheckInfo checkInfo : checkInfoList) {
            if (checkInfo.status)
                return true;
        }

        return false;
    }

    private boolean hasOnlyFailedChecks() {
        if (getCheckCount() < LinkMonitorService.maxChecks && getCheckCount() > 0 && ! hasSuccessCheck()) {
            // In the case when we don't have enough checks, but have more
            // than one check and they all failed - mark link as failed
            return true;
        }

        return false;
    }

    private boolean hasEnoughChecks() {
        return getCheckCount() >= LinkMonitorService.maxChecks;
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

    boolean hasEnoughSuccessChecks() {
        int successChecks = 0;
        for (final CheckInfo checkInfo : checkInfoList) {
            if (checkInfo.status) {
                successChecks++;
            }
        }

        int percentSuccess = 100 * successChecks / getCheckCount();

        return percentSuccess >= LinkMonitorService.percentWorkingThreshold;
    }

    public LinkMonitorService.Status evaluateStatus() {
        if (hasOnlyFailedChecks()) {
            return LinkMonitorService.Status.FAILED;
        }

        if (!hasEnoughChecks()) {
            return LinkMonitorService.Status.UNKNOWN;
        }

        if (!isFresh()) {
            return LinkMonitorService.Status.UNKNOWN;
        }

        if(hasEnoughSuccessChecks()) {
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
