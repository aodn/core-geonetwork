package org.fao.geonet.monitor.link;

import jeeves.resources.dbms.Dbms;
import org.apache.log4j.Logger;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataRecordInfo {
    private static Logger logger = Logger.getLogger(MetadataRecordInfo.class);

    private String uuid;

    private List<LinkInfo> linkInfoList;

    public static String ONLINE_RESOURCES_XPATH = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource";
    public static String PROTOCOL_XPATH = "gmd:protocol/gco:CharacterString";

    private static final Map<String, Class> linkCheckerMap = new HashMap<String, Class>();

    private LinkMonitorService.Status status = LinkMonitorService.Status.UNKNOWN;

    static {
        linkCheckerMap.put("OGC:WMS-1.1.1-http-get-map", LinkCheckerWms.class);
        linkCheckerMap.put("OGC:WFS-1.0.0-http-get-capabilities", LinkCheckerWfs.class);
        linkCheckerMap.put("IMOS:AGGREGATION--bodaac", LinkCheckerWfs.class);
        linkCheckerMap.put("WWW:LINK-1.0-http--metadata-URL", LinkCheckerDefault.class);
        linkCheckerMap.put("WWW:DOWNLOAD-1.0-http--downloadother", LinkCheckerDefault.class);
    }

    public MetadataRecordInfo(LinkMonitorService linkMonitorService, String uuid) {
        this.uuid = uuid;
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
                        LinkCheckerInterface linkChecker = getCheckerForLinkType(protocol, onlineResource);
                        if (linkChecker == null) {
                            logger.debug(String.format("Cannot find checker for '%s'", protocol));
                        }
                        else {
                            logger.info(String.format("Configuring checker '%s' for '%s'", linkChecker.toString(), protocol));
                            linkInfoList.add(new LinkInfo(linkChecker));
                        }
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
        logger.debug(String.format("Checking '%s'", uuid));
        LinkMonitorService.Status prevStatus = getStatus();
        for (final LinkInfo linkInfo : linkInfoList) {
            linkInfo.check();
        }
        LinkMonitorService.Status newStatus = getStatus();
        ReportStatusChange(prevStatus, newStatus);
        status = evaluateStatus();
    }

    private void ReportStatusChange(LinkMonitorService.Status prevStatus, LinkMonitorService.Status newStatus) {
        if (prevStatus == newStatus) {
            return;
        }

        logger.info(String.format("Record '%s' changes status from '%s' to '%s'", uuid, prevStatus, newStatus));

        if (newStatus == LinkMonitorService.Status.FAILED) {
            for (final LinkInfo linkInfo : linkInfoList) {
                if (linkInfo.getStatus() != LinkMonitorService.Status.WORKING) {
                    logger.info(String.format("Link '%s' is in state '%s'", linkInfo.toString(), linkInfo.getStatus()));
                }
            }
        }
    }

    private static LinkCheckerInterface getCheckerForLinkType(String linkType, final Element onlineResource) {
        Class linkCheckerClass = linkCheckerMap.get(linkType);
        if (linkCheckerClass != null) {
            try {
                LinkCheckerInterface linkCheckerInterface = (LinkCheckerInterface) linkCheckerClass.newInstance();
                linkCheckerInterface.setOnlineResource(onlineResource);
                return linkCheckerInterface;
            } catch (Exception e) {
                logger.info(e);
            }
        }

        return null;
    }
}
