package org.fao.geonet.monitor.link;

import org.apache.log4j.Logger;
import org.jdom.Element;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;

public class LinkCheckerWmsGetCapabilities extends LinkCheckerDefault {

    private static Logger logger = Logger.getLogger(LinkCheckerWmsGetCapabilities.class);

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        url += "?service=wms&version=1.1.1&request=GetCapabilities";
    }

    @Override
    public boolean check() {

        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(LinkMonitorService.timeout * 1000);
            connection.setReadTimeout(LinkMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

            logger.debug(String.format("%s -> %d", url, connection.getResponseCode()));

            if(connection.getResponseCode() != 200) {
                logger.info(String.format("link broken uuid='%s', url='%s', error='bad response code %d'",
                        this.uuid, this.url, connection.getResponseCode()));
                return false;
            }

            if (connection.getContentType().equals("application/vnd.ogc.wms_xml")) {
                InputStream is = null;
                try {
                    is = connection.getInputStream();
                    LinkCheckerUtils.parseXML(is);
                } finally {
                    if(is != null) {
                        is.close();
                    }
                }
                return true;
            } else {
                logger.info(String.format("link broken uuid='%s', url='%s', error='unexpected content-type'", this.uuid, this.url));
                return false;
            }

        } catch (Exception e) {

            logger.info(String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                    uuid, url, e.getMessage(), LinkCheckerUtils.exceptionToString(e)));
        }

        return false;
    }
}