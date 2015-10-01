package org.fao.geonet.monitor.link;

import jeeves.server.ServiceConfig;
import jeeves.server.resources.ResourceManager;
import org.fao.geonet.GeonetContext;
import org.springframework.context.ApplicationContext;

public interface LinkMonitorInterface extends Runnable {
    public boolean isHealthy(String recordUuid);

    public void init(
        ApplicationContext applicationContext,
        ResourceManager resourceManager,
        GeonetContext geonetContext,
        ServiceConfig serviceConfig
    );
}
