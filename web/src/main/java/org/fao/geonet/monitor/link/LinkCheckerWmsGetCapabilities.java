package org.fao.geonet.monitor.link;

import org.jdom.Element;


public class LinkCheckerWmsGetCapabilities  extends LinkCheckerDefault {
    //private static String WMS_GET_MAP = "?service=WMS&request=GetMap&version=1.1.1&format=image/png&bbox=-180,-90,180,90&srs=EPSG:4326&width=1&height=1&STYLES=&layers=";
    //http://geoserver-123.aodn.org.au/geoserver/ows?service=wms&version=1.1.1&request=GetCapabilities

    @Override
    public void setOnlineResource(final Element onlineResource) {
        super.setOnlineResource(onlineResource);
        //String name = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.NAME_XPATH);
        url += "?service=wms&version=1.1.1&request=GetCapabilities";
    }
}