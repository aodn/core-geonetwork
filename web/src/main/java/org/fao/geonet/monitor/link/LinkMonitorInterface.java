package org.fao.geonet.monitor.link;

import jeeves.server.ServiceConfig;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.GeonetContext;

public interface LinkMonitorInterface extends Runnable {
    public boolean isHealthy(String recordUuid);

    public void init(ResourceManager resourceManager, GeonetContext geonetContext, ServiceConfig serviceConfig);
}
