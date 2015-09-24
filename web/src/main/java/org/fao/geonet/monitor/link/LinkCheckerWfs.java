package org.fao.geonet.monitor.link;

import org.jdom.Element;

public class LinkCheckerWfs extends LinkCheckerDefault {
    private static String WFS_GET_FEATURES = "?service=WFS&version=1.0.0&request=GetFeature&maxFeatures=1&outputFormat=csv&typeName=";

    @Override
    public void setOnlineResource(final Element onlineResource) {
        super.setOnlineResource(onlineResource);
        String name = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.NAME_XPATH);
        url += WFS_GET_FEATURES + name;
    }
}
