package org.fao.geonet.monitor.link;

import org.jdom.Element;

import java.util.Set;

public class LinkCheckerDefault implements LinkCheckerInterface {

    protected Element onlineResource = null;

    protected String url = "";

    protected Set<String> linkTypes;

    protected String lastErrorMsg = null;

    @Override
    public void setOnlineResource(final Element onlineResource) {
        this.onlineResource = onlineResource;
        url = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.URL_XPATH);
    }

    @Override
    public boolean canHandle(String linkType) {
        return linkTypes.contains(linkType);
    }

    @Override
    public boolean check() {
        try {
            return LinkCheckerUtils.checkHttpUrl(url);
        } catch (Exception e) {
            lastErrorMsg = e.getMessage();
            return false;
        }
    }

    @Override
    public String getLastErrorMsg() {
        return lastErrorMsg;
    }

    @Override
    public String toString() {
        return url;
    }

    public void setLinkTypes(Set<String> linkTypes) {
        this.linkTypes = linkTypes;
    }
}
