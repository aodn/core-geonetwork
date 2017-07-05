package org.fao.geonet.monitor.onlineresource;

import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;

public class OnlineResourceCheckerWmsGetCapabilities extends OnlineResourceCheckerDefault {

    private static Logger logger = Logger.getLogger(OnlineResourceCheckerWmsGetCapabilities.class);

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        url += "?service=wms&version=1.1.1&request=GetCapabilities";
    }

    @Override
    public boolean check() {

        InputStream is = null;
        HttpURLConnection connection;
        boolean check = true;

        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setReadTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

            logger.debug(String.format("%s -> %d", url, connection.getResponseCode()));

            if(connection.getResponseCode() != 200) {
                logger.info(String.format("link broken uuid='%s', url='%s', error='bad response code %d'",
                        this.uuid, this.url, connection.getResponseCode()));
                check = false;
            } else {
                is = connection.getInputStream();
                OnlineResourceCheckerUtils.parseXML(is);
            }
        } catch (Exception e) {
            logger.info(String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                    uuid, url, e.getMessage(), OnlineResourceCheckerUtils.exceptionToString(e)));
            check = false;
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.info(String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                            uuid, url, e.getMessage(), OnlineResourceCheckerUtils.exceptionToString(e)));
                    check = false;
                }
            }
        }

        return check;
    }
}
