package org.fao.geonet.monitor.onlineresource;

import org.jdom.Element;

public interface OnlineResourceCheckerInterface {
    public void setOnlineResource(String uuid, Element onlineResource);

    public boolean canHandle(String onlineResourceType);

    public CheckResult check();

    public String toString();
}
