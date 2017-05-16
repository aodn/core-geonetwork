package org.fao.geonet.monitor.onlineresource;

import org.jdom.Element;

public class OnlineResourceCheckerWms extends OnlineResourceCheckerDefault {
    private static String WMS_GET_MAP = "?service=WMS&request=GetMap&version=1.1.1&format=image/png&bbox=-180,-90,180,90&srs=EPSG:4326&width=1&height=1&STYLES=&layers=";

    @Override
    public void setOnlineResource(String uuuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        String name = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.NAME_XPATH);
        url += WMS_GET_MAP + name;
    }
}
