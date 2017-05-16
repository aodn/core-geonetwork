package org.fao.geonet.monitor.onlineresource;

import org.jdom.Element;

public class OnlineResourceCheckerNcwmsAbom extends OnlineResourceCheckerDefault {
    private static String ABOM_GET_MAP = "?service=ncwms&request=GetMap&version=1.1.1&format=image/png&bbox=-180,-90,180,90&srs=EPSG:4326&width=1&height=1&STYLES=&LAYERS=";

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        String name = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.NAME_XPATH);
        url += ABOM_GET_MAP + name;
    }
}
