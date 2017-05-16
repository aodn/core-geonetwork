package org.fao.geonet.monitor.onlineresource;

import org.jdom.Element;

public class OnlineResourceCheckerWfs extends OnlineResourceCheckerDefault {
    private static String WFS_GET_FEATURES = "?service=WFS&version=1.0.0&request=GetFeature&maxFeatures=1&outputFormat=csv&typeName=";

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        String name = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.NAME_XPATH);
        url += WFS_GET_FEATURES + name;
    }
}
