package org.fao.geonet.monitor.link;

import org.fao.geonet.monitor.exception.LinkCheckerException;
import org.jdom.Element;

public interface LinkCheckerInterface {
    void setOnlineResource(String uuid, Element onlineResource);

    boolean canHandle(String linkType);

    boolean check();

    LinkCheckerException getLastException();

    String toString();
}
