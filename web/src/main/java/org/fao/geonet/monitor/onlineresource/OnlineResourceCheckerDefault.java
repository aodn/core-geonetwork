package org.fao.geonet.monitor.onlineresource;

import org.jdom.Element;

import java.util.Set;

public class OnlineResourceCheckerDefault implements OnlineResourceCheckerInterface {

    protected String uuid = null;

    protected Element onlineResource = null;

    protected String url = "";

    protected Set<String> onlineResourceTypes;

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        this.uuid = uuid;
        this.onlineResource = onlineResource;
        url = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.URL_XPATH);
    }

    @Override
    public boolean canHandle(String onlineResourceType) {
        return onlineResourceTypes.contains(onlineResourceType);
    }

    @Override
    public boolean check() {
        return OnlineResourceCheckerUtils.checkHttpUrl(this.uuid, this.url);
    }

    @Override
    public String toString() {
        return url;
    }

    public void setOnlineResourceTypes(Set<String> onlineResourceTypes) {
        this.onlineResourceTypes = onlineResourceTypes;
    }
}
