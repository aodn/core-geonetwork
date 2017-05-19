package org.fao.geonet.monitor.link;

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

    private List<LinkInfo> linkInfoList;

    public static String ONLINE_RESOURCES_XPATH = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource";
    public static String PROTOCOL_XPATH = "gmd:protocol/gco:CharacterString";

    private LinkMonitorService.Status status = LinkMonitorService.Status.UNKNOWN;

    public MetadataRecordInfo(LinkMonitorService linkMonitorService, String uuid, String title, long lastUpdated) {
        this.uuid = uuid;
        this.title = title;
        this.lastUpdated = lastUpdated;
        linkInfoList = new ArrayList<LinkInfo>();
        getOnlineResources(linkMonitorService.getDocumentForUuid(uuid));
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
                        addCheckers(protocol, onlineResource, this.uuid, this.linkInfoList);
                    }
                }
            }
        } catch (JDOMException e) {
            logger.info(e);
        }
    }

    public static boolean isHealthy(boolean unknownAsWorking, LinkMonitorService.Status status) {
        if (status == LinkMonitorService.Status.UNKNOWN) {
            return unknownAsWorking;
        } else {
            return status == LinkMonitorService.Status.WORKING;
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

    private LinkMonitorService.Status getStatus() {
        return status;
    }

    private LinkMonitorService.Status evaluateStatus() {
        List<LinkMonitorService.Status> statusList = new ArrayList<LinkMonitorService.Status>();
        for (final LinkInfo linkInfo : linkInfoList) {
            statusList.add(linkInfo.getStatus());
        }

        return evaluateStatus(statusList);
    }

    public static LinkMonitorService.Status evaluateStatus(List<LinkMonitorService.Status> statusList) {
        if (statusList.contains(LinkMonitorService.Status.FAILED)) {
            return LinkMonitorService.Status.FAILED;
        } else if (statusList.contains(LinkMonitorService.Status.UNKNOWN)) {
            return LinkMonitorService.Status.UNKNOWN;
        } else {
            return LinkMonitorService.Status.WORKING;
        }
    }

    public void check() {
        LinkMonitorService.Status prevStatus = getStatus();
        for (final LinkInfo linkInfo : linkInfoList) {
            linkInfo.check();
        }
        status = evaluateStatus();
        ReportStatusChange(prevStatus, getStatus());
    }

    private void ReportStatusChange(LinkMonitorService.Status prevStatus, LinkMonitorService.Status newStatus) {
        if (prevStatus == newStatus) {
            logger.info(String.format("Record uuid='%s', title='%s' status is unchanged '%s'", uuid, title, newStatus));
            return;
        }

        logger.info(String.format("Record uuid='%s' title='%s' changes status from '%s' to '%s'", uuid, title, prevStatus, newStatus));

        if (newStatus == LinkMonitorService.Status.FAILED) {
            for (final LinkInfo linkInfo : linkInfoList) {
                if (linkInfo.getStatus() != LinkMonitorService.Status.WORKING) {
                    logger.info(String.format("Link for uuid='%s', title='%s', '%s' is in state '%s' error='%s' stack='%s'", uuid, title, linkInfo.toString(), linkInfo.getStatus(), linkInfo.getLastException().getMessage(), LinkCheckerUtils.exceptionToString(linkInfo.getLastException())));
                }
            }
        }
    }

    private static void addCheckers(String linkType, final Element onlineResource, String uuid, List<LinkInfo> linkInfoList) {

        final Map<String, LinkCheckerInterface> linkCheckerClasses =
                LinkMonitorService.getApplicationContext().getBeansOfType(LinkCheckerInterface.class);

        int count = 0;

        for (final String beanId : linkCheckerClasses.keySet()) {
            if (linkCheckerClasses.get(beanId).canHandle(linkType)) {
                try {
                    Class linkCheckerClass = linkCheckerClasses.get(beanId).getClass();
                    LinkCheckerInterface linkCheckerInterface = (LinkCheckerInterface) linkCheckerClass.newInstance();
                    linkCheckerInterface.setOnlineResource(uuid, onlineResource);

                    linkInfoList.add(new LinkInfo(linkCheckerInterface));
                    logger.debug(String.format("Configuring checker '%s' for '%s'", linkCheckerInterface.toString(), linkType));
                    ++count;
                } catch (Exception e) {
                    logger.error("Error could not find the onlineResource: ", e);
                }
            }
        }

        if(count == 0) {
            logger.debug(String.format("Cannot find checker for '%s'", linkType));
        }
    }
}
