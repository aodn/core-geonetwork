package org.fao.geonet.monitor.link;

import org.jdom.Element;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class CheckInfo {
    public boolean status;
    public long timestamp;

    public CheckInfo(Element onlineResource, LinkCheckerInterface linkChecker) {
        this.status = linkChecker.check(onlineResource);
        this.timestamp = System.currentTimeMillis() / 1000l;
    }
}
