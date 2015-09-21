package org.fao.geonet.monitor.link;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LinkInfo {

    private List<CheckInfo> checkInfoList;

    private final Element onlineResource;

    private final LinkCheckerInterface linkChecker;

    public LinkInfo(Element onlineResource, LinkCheckerInterface linkChecker) {
        checkInfoList = Collections.synchronizedList(new ArrayList<CheckInfo>());
        this.onlineResource = onlineResource;
        this.linkChecker = linkChecker;
    }

    public int getCheckCount() {
        return checkInfoList.size();
    }

    public LinkMonitorService.Status getStatus() {
        if (getCheckCount() < LinkMonitorService.maxChecks) {
            if (getCheckCount() > 0 && ! hasSuccessCheck()) {
                // In the case when we don't have enough checks, but have more
                // than one check and they all failed - mark link as failed
                return LinkMonitorService.Status.FAILED;
            } else {
                // Not enough information to decide whether the link is online
                return LinkMonitorService.Status.UNKNOWN;
            }
        }

        long now = System.currentTimeMillis() / 1000l;
        long lastCheck = checkInfoList.get(getCheckCount() - 1).timestamp;
        if (now - lastCheck > LinkMonitorService.freshness) {
            LinkMonitorService.getLogger().debug(String.format("Last test is too old, ('%s' seconds ago)", now - lastCheck));
            return LinkMonitorService.Status.UNKNOWN;
        }

        int successChecks = 0;
        for (final CheckInfo checkInfo : checkInfoList) {
            if (checkInfo.status) {
                successChecks++;
            }
        }

        int percentSuccess = 100 * successChecks / getCheckCount();

        if (percentSuccess >= LinkMonitorService.percentWorking) {
            return LinkMonitorService.Status.WORKING;
        } else {
            return LinkMonitorService.Status.FAILED;
        }
    }

    private boolean hasSuccessCheck() {
        for (final CheckInfo checkInfo : checkInfoList) {
            if (checkInfo.status)
                return true;
        }

        return false;
    }

    private void truncateCheckList() {
        // Leave just the last maxChecks checks
        while (getCheckCount() > LinkMonitorService.maxChecks) {
            checkInfoList.remove(0);
        }
    }

    private void addCheckInfo(CheckInfo checkInfo) {
        checkInfoList.add(checkInfo);
    }

    public void check() {
        checkInfoList.add(new CheckInfo(onlineResource, linkChecker));
        truncateCheckList();
    }

    @Override
    public String toString() {
        return linkChecker.toString(onlineResource);
    }
}
