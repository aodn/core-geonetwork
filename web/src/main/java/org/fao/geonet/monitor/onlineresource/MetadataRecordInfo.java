package org.fao.geonet.monitor.onlineresource;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetadataRecordInfo {
    private static Logger logger = Logger.getLogger(MetadataRecordInfo.class);

    private final String uuid;
    private String title;
    private long lastUpdated;

    private List<OnlineResourceInfo> onlineResourceInfoList;

    public static String ONLINE_RESOURCES_XPATH = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource";
    public static String PROTOCOL_XPATH = "gmd:protocol/gco:CharacterString";

    private OnlineResourceMonitorService.Status status = OnlineResourceMonitorService.Status.UNKNOWN;

    public MetadataRecordInfo(OnlineResourceMonitorService onlineResourceService, String uuid, String title, long lastUpdated) {
        this.uuid = uuid;
        this.title = title;
        this.lastUpdated = lastUpdated;
        onlineResourceInfoList = new ArrayList<OnlineResourceInfo>();
        getOnlineResources(onlineResourceService.getDocumentForUuid(uuid));
    }

    public void getOnlineResources(Element element) {
        try {
            XPath pOnlineResources = XPath.newInstance(ONLINE_RESOURCES_XPATH);
            for (final Object onlineResourceObject : pOnlineResources.selectNodes(element)) {
                final Element onlineResource = (Element) onlineResourceObject;

                XPath pProtocol = XPath.newInstance(PROTOCOL_XPATH);
                Element protocolElement = (Element) pProtocol.selectSingleNode(onlineResource);

                if (protocolElement != null) {
                    String protocol = protocolElement.getText();

                    if (protocol != null) {
                        addCheckers(protocol, onlineResource, this.uuid, this.onlineResourceInfoList);
                    }
                }
            }
        } catch (JDOMException e) {
            logger.info(e);
        }
    }

    public static boolean isHealthy(boolean unknownAsWorking, OnlineResourceMonitorService.Status status) {
        if (status == OnlineResourceMonitorService.Status.UNKNOWN) {
            return unknownAsWorking;
        } else {
            return status == OnlineResourceMonitorService.Status.WORKING;
        }
    }

    public boolean isHealthy(boolean unknownAsWorking) {
        return isHealthy(unknownAsWorking, getStatus());
    }

    public long getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }

    public String getTitle() { return title;}

    private OnlineResourceMonitorService.Status getStatus() {
        return status;
    }

    private OnlineResourceMonitorService.Status evaluateStatus() {
        List<OnlineResourceMonitorService.Status> statusList = new ArrayList<OnlineResourceMonitorService.Status>();
        for (final OnlineResourceInfo onlineResourceInfo : onlineResourceInfoList) {
            statusList.add(onlineResourceInfo.getStatus());
        }

        return evaluateStatus(statusList);
    }

    public static OnlineResourceMonitorService.Status evaluateStatus(List<OnlineResourceMonitorService.Status> statusList) {
        if (statusList.contains(OnlineResourceMonitorService.Status.FAILED)) {
            return OnlineResourceMonitorService.Status.FAILED;
        } else if (statusList.contains(OnlineResourceMonitorService.Status.UNKNOWN)) {
            return OnlineResourceMonitorService.Status.UNKNOWN;
        } else {
            return OnlineResourceMonitorService.Status.WORKING;
        }
    }

    public void check() {
        OnlineResourceMonitorService.Status prevStatus = getStatus();
        for (final OnlineResourceInfo onlineResourceInfo : onlineResourceInfoList) {
            onlineResourceInfo.check();
        }
        status = evaluateStatus();
        ReportStatusChange(prevStatus, getStatus());
    }

    private void ReportStatusChange(OnlineResourceMonitorService.Status prevStatus, OnlineResourceMonitorService.Status newStatus) {
        if (prevStatus == newStatus) {
            logger.info(String.format("Record uuid='%s', title='%s' status is unchanged '%s'", uuid, title, newStatus));
            return;
        }

        //  Get a list of the error messages to report (if any)
        StringBuilder errorSummary = new StringBuilder();
        for (final OnlineResourceInfo onlineResourceInfo : onlineResourceInfoList) {
            if(onlineResourceInfo.getCheckInfoList() != null) {
                List<CheckInfo> infoList = onlineResourceInfo.getCheckInfoList();
                for(CheckInfo currentCheckInfo : infoList) {
                    CheckResult checkResult = currentCheckInfo.getCheckResult();
                    if(!checkResult.isSuccessful()) {
                        if(checkResult.getResultReason() != null) {
                            errorSummary.append("[" + checkResult.getResultReason() + "] ");
                        }
                    }
                }
            }
        }

        String errorString = errorSummary.toString();
        if(errorString.trim().length() > 0) {
            logger.info(String.format("Record uuid='%s' title='%s' changes status from '%s' to '%s'. Reason/s: %s", uuid, title, prevStatus, newStatus, errorString));
        } else {
            logger.info(String.format("Record uuid='%s' title='%s' changes status from '%s' to '%s'", uuid, title, prevStatus, newStatus));
        }

        for (final OnlineResourceInfo onlineResourceInfo : onlineResourceInfoList) {
            logger.debug(String.format(
                "Link for uuid='%s', title='%s', '%s' is in state '%s': %d of last %d checks failed",
                uuid, title, onlineResourceInfo.toString(), onlineResourceInfo.getStatus(),
                onlineResourceInfo.getFailureCount(), onlineResourceInfo.getCheckCount()
            ));
        }
    }

    private static void addCheckers(String onlineResourceType, final Element onlineResource, String uuid, List<OnlineResourceInfo> onlineResourceInfoList) {
        final Map<String, OnlineResourceCheckerInterface> onlineResourceCheckerClasses =
                OnlineResourceMonitorService.getApplicationContext().getBeansOfType(OnlineResourceCheckerInterface.class);

        int count = 0;

        for (final String beanId : onlineResourceCheckerClasses.keySet()) {
            if (onlineResourceCheckerClasses.get(beanId).canHandle(onlineResourceType)) {
                try {
                    Class onlineResourceCheckerClass = onlineResourceCheckerClasses.get(beanId).getClass();
                    OnlineResourceCheckerInterface onlineResourceCheckerInterface = (OnlineResourceCheckerInterface) onlineResourceCheckerClass.newInstance();
                    onlineResourceCheckerInterface.setOnlineResource(uuid, onlineResource);

                    onlineResourceInfoList.add(new OnlineResourceInfo(onlineResourceCheckerInterface));
                    logger.debug(String.format("Configuring checker '%s' for '%s'", onlineResourceCheckerInterface.toString(), onlineResourceType));
                    ++count;
                }
                catch (Exception e) {
                    logger.error("Error could not find the onlineResource: ", e);
                }
            }
        }

        if (count == 0) {
            logger.debug(String.format("Cannot find checker for '%s'", onlineResourceType));
        }
    }
}
