package org.fao.geonet.monitor.link;

import org.jdom.Element;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class LinkCheckerWms implements LinkCheckerInterface {
    private static String WMS_GET_MAP = "?service=WMS&request=GetMap&version=1.1.1&format=image/png&bbox=-180,-90,180,90&srs=EPSG:4326&width=1&height=1&STYLES=&layers=";

    private String getCheckUrl(Element onlineResource) {
        String url = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.URL_XPATH);
        String name = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.NAME_XPATH);

        if (url != null && name != null) {
            return url + WMS_GET_MAP + name;
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
