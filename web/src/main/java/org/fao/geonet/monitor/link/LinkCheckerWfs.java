package org.fao.geonet.monitor.link;

import org.jdom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LinkCheckerWfs implements LinkCheckerInterface {
    private static String WFS_GET_FEATURES = "?service=WFS&version=1.0.0&request=GetFeature&maxFeatures=1&outputFormat=csv&typeName=";

    private String getCheckUrl(Element onlineResource) {
        String url = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.URL_XPATH);
        String name = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.NAME_XPATH);

        if (url != null && name != null) {
            return url + WFS_GET_FEATURES + name;
        }

        return null;
    }

    @Override
    public boolean check(Element onlineResource) {
        String checkUrl = getCheckUrl(onlineResource);
        if (checkUrl != null)
            return LinkCheckerUtils.checkHttpUrl(checkUrl);

        return true;
    }

    @Override
    public String toString(Element onlineResource) {
        return getCheckUrl(onlineResource);
    }
}
