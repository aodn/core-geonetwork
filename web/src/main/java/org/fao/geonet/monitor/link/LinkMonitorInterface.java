package org.fao.geonet.monitor.link;

import jeeves.interfaces.Logger;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.GeonetContext;

public class LinkMonitorInterface {
    private static ResourceManager resourceManager;
    private static GeonetContext geonetContext;
    private static Logger logger;
    private static long reindexInterval;

    public LinkMonitorInterface() {
    }

    public void init(ResourceManager resourceManager, GeonetContext geonetContext, Logger logger, long reindexInterval) {
        this.resourceManager = resourceManager;
        this.geonetContext = geonetContext;
        this.logger = logger;
        this.reindexInterval = reindexInterval;
    }

    public static ResourceManager getResourceManager() {
        return resourceManager;
    }

    public static GeonetContext getGeonetContext() {
        return geonetContext;
    }

    public static Logger getLogger() {
        return logger;
    }

    public static long getReindexInterval() {
        return reindexInterval;
    }

    public boolean isHealthy(String recordUuid) {
        return true;
    }

    public void check() {}
}