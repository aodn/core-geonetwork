package org.fao.geonet.monitor.link;

import org.jdom.Element;

public interface LinkCheckerInterface {
    public void setOnlineResource(Element onlineResource);

    public boolean check();

    public String toString();
}
