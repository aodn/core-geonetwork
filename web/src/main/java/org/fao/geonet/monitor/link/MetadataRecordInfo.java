package org.fao.geonet.monitor.link;

import jeeves.resources.dbms.Dbms;
import org.fao.geonet.constants.Geonet;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataRecordInfo {
    private String uuid;

    private List<LinkInfo> linkInfoList;

    public static String ONLINE_RESOURCES_XPATH = "gmd:distributionInfo/gmd:MD_Distribution/gmd:transferOptions/gmd:MD_DigitalTransferOptions/gmd:onLine/gmd:CI_OnlineResource";
    public static String PROTOCOL_XPATH = "gmd:protocol/gco:CharacterString";

    private static final Map<String, Class> linkCheckerMap = new HashMap<String, Class>();
    static {
        linkCheckerMap.put("OGC:WMS-1.1.1-http-get-map", LinkCheckerWms.class);
        linkCheckerMap.put("OGC:WFS-1.0.0-http-get-capabilities", LinkCheckerWfs.class);
        linkCheckerMap.put("IMOS:AGGREGATION--bodaac", LinkCheckerWfs.class);
        linkCheckerMap.put("WWW:LINK-1.0-http--metadata-URL", LinkCheckerDefault.class);
        linkCheckerMap.put("WWW:DOWNLOAD-1.0-http--downloadother", LinkCheckerDefault.class);
    }

    public MetadataRecordInfo(String uuid) {
        this.uuid = uuid;
        linkInfoList = new ArrayList<LinkInfo>();
        getOnlineResources(getDocumentForUuid(uuid));
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
                        LinkCheckerInterface linkChecker = getCheckerForLinkType(protocol);
                        if (linkChecker == null) {
                            LinkMonitorService.getLogger().debug(String.format("Cannot find checker for '%s'", protocol));
                        }
                        else {
                            LinkMonitorService.getLogger().info(String.format("Configuring checker '%s' for '%s'", linkChecker.toString(), protocol));
                            linkInfoList.add(new LinkInfo(onlineResource, linkChecker));
                        }
                    }
                }

            }
        } catch (JDOMException e) {
            e.printStackTrace();
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

    private Element getDocumentForUuid(String uuid) {
        try {
            Dbms dbms = (Dbms) LinkMonitorService.getResourceManager().open(Geonet.Res.MAIN_DB);
            String id = LinkMonitorService.getGeonetContext().getDataManager().getMetadataId(dbms, uuid);
            return LinkMonitorService.getGeonetContext().getDataManager().getMetadataIgnorePermissions(dbms, id);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private LinkMonitorService.Status getStatus() {
        List<LinkMonitorService.Status> statusList = new ArrayList<LinkMonitorService.Status>();
        for (final LinkInfo linkInfo : linkInfoList) {
            statusList.add(linkInfo.getStatus());
        }

        return getStatus(statusList);
    }

    public static LinkMonitorService.Status getStatus(List<LinkMonitorService.Status> statusList) {
        if (statusList.contains(LinkMonitorService.Status.FAILED)) {
            return LinkMonitorService.Status.FAILED;
        } else if (statusList.contains(LinkMonitorService.Status.UNKNOWN)) {
            return LinkMonitorService.Status.UNKNOWN;
        } else {
            return LinkMonitorService.Status.WORKING;
        }
    }

    public void check() {
        LinkMonitorService.getLogger().debug(String.format("Checking '%s'", uuid));
        LinkMonitorService.Status prevStatus = getStatus();
        for (final LinkInfo linkInfo : linkInfoList) {
            linkInfo.check();
        }
        LinkMonitorService.Status newStatus = getStatus();
        ReportStatusChange(prevStatus, newStatus);
    }

    private void ReportStatusChange(LinkMonitorService.Status prevStatus, LinkMonitorService.Status newStatus) {
        if (prevStatus == newStatus)
            return;

        LinkMonitorService.getLogger().info(String.format("Record '%s' changes status from '%s' to '%s'", uuid, prevStatus, newStatus));

        if (newStatus == LinkMonitorService.Status.FAILED) {
            for (final LinkInfo linkInfo : linkInfoList) {
                if (linkInfo.getStatus() != LinkMonitorService.Status.WORKING) {
                    LinkMonitorService.getLogger().info(String.format("Link '%s' is in state '%s'", linkInfo.toString(), linkInfo.getStatus()));
                }
            }
        }
    }

    private static LinkCheckerInterface getCheckerForLinkType(String linkType) {
        Class linkCheckerClass = linkCheckerMap.get(linkType);
        if (linkCheckerClass != null) {
            try {
                return (LinkCheckerInterface) linkCheckerClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return null;
    }
}
