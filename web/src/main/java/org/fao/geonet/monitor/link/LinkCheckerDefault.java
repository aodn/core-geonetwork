package org.fao.geonet.monitor.link;

import org.jdom.Element;

public class LinkCheckerDefault implements LinkCheckerInterface {
    private String getCheckUrl(Element onlineResource) {
        return LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.URL_XPATH);
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
