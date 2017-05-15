package org.fao.geonet.monitor.link;

import org.jdom.Element;

public interface LinkCheckerInterface {
    public void setOnlineResource(String uuid, Element onlineResource);

    public boolean canHandle(String linkType);

    public boolean check();

    public String toString();
}
