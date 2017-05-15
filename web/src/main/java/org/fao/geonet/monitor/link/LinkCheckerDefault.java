package org.fao.geonet.monitor.link;

import org.jdom.Element;

import java.util.Set;

public class LinkCheckerDefault implements LinkCheckerInterface {

    protected String uuid = null;

    protected Element onlineResource = null;

    protected String url = "";

    protected Set<String> linkTypes;

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        this.uuid = uuid;
        this.onlineResource = onlineResource;
        url = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.URL_XPATH);
    }

    @Override
    public boolean canHandle(String linkType) {
        return linkTypes.contains(linkType);
    }

    @Override
    public boolean check() {
        return LinkCheckerUtils.checkHttpUrl(this.uuid, this.url);
    }

    @Override
    public String toString() {
        return url;
    }

    public void setLinkTypes(Set<String> linkTypes) {
        this.linkTypes = linkTypes;
    }
}
