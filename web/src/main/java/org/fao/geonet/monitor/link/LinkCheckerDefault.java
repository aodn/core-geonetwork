package org.fao.geonet.monitor.link;

import org.jdom.Element;

public class LinkCheckerDefault implements LinkCheckerInterface {

    protected Element onlineResource = null;

    protected String url = "";

    @Override
    public void setOnlineResource(final Element onlineResource) {
        this.onlineResource = onlineResource;
        url = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.URL_XPATH);
    }

    @Override
    public boolean check() {
        return LinkCheckerUtils.checkHttpUrl(url);
    }

    @Override
    public String toString() {
        return url;
    }
}
