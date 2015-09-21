package org.fao.geonet.monitor.link;

import org.jdom.Element;

public interface LinkCheckerInterface {
    public boolean check(Element onlineResource);

    public String toString(Element onlineResource);
}
