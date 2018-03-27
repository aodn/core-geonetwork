package org.fao.geonet.monitor.onlineresource;

import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class OnlineResourceCheckerWmsGetCapabilities extends OnlineResourceCheckerDefault {

    private static Logger logger = Logger.getLogger(OnlineResourceCheckerWmsGetCapabilities.class);

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        url += "?service=wms&version=1.1.1&request=GetCapabilities";
    }

    @Override
    public CheckResult check() {

        InputStream is = null;
        HttpURLConnection connection;
        long start = System.currentTimeMillis();

        try {
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setReadTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

            logger.debug(String.format("%s -> %d", url, connection.getResponseCode()));

            if(connection.getResponseCode() != 200) {
                String errorMessage = String.format("link broken uuid='%s', url='%s', error='bad response code %d'",
                        this.uuid, this.url, connection.getResponseCode());
                logger.info(errorMessage);
                return new CheckResult(CheckResultEnum.FAIL, errorMessage);
            } else {
                is = connection.getInputStream();
                OnlineResourceCheckerUtils.parseXML(is);
                return new CheckResult(CheckResultEnum.SUCCESS, null);
            }
        } catch (Exception e) {
            String errorMessage = String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                    uuid, url, e.getMessage(), OnlineResourceCheckerUtils.exceptionToString(e));
            logger.info(errorMessage);
            return new CheckResult(CheckResultEnum.FAIL, errorMessage);
        } finally {
            if(is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    String errorMessage = String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                            uuid, url, e.getMessage(), OnlineResourceCheckerUtils.exceptionToString(e));
                    logger.info(errorMessage);
                    return new CheckResult(CheckResultEnum.FAIL, errorMessage);
                }
            }
            logger.info(String.format("link uuid='%s', url='%s', took '%s' seconds",
                    uuid, url, (System.currentTimeMillis() - start) / 1000));
        }
    }
}
