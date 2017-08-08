package org.fao.geonet.monitor.onlineresource;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.fao.geonet.monitor.onlineresource.OnlineResourceCheckerUtils.parseXML;

public class OnlineResourceCheckerWmsFilters extends OnlineResourceCheckerDefault {

    private static Logger logger = Logger.getLogger(OnlineResourceCheckerWmsFilters.class);

    String name;
    String server;
    private static String WMS_GET_FILTERS = "?service=layerFilters&request=enabledFilters&version=1.0.0&layer=";
    private static String WMS_GET_FILTER_VALUES = "%s?service=layerFilters&request=uniqueValues&version=1.0.0&layer=%s&propertyName=%s";

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        name = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.NAME_XPATH);
        server = url;
        url += WMS_GET_FILTERS + name;
    }

    private String getFilterValuesUrl(String property) {
        return String.format(WMS_GET_FILTER_VALUES, server, name, property);
    }

    private HttpURLConnection getConnection(String theUrl) throws Exception {

        HttpURLConnection connection;
        connection = (HttpURLConnection) (new URL(theUrl)).openConnection();
        connection.setConnectTimeout(OnlineResourceMonitorService.timeout * 1000);
        connection.setReadTimeout(OnlineResourceMonitorService.timeout * 1000);
        connection.setRequestMethod("GET");
        connection.connect();

        return connection;
    }

    private boolean checkFilterValues(String theUrl) {

        boolean check = true;
        long start = System.currentTimeMillis();

        try {
            HttpURLConnection connection = getConnection(theUrl);
            if (connection.getResponseCode() != 200) {
                Integer resCode = connection.getResponseCode();
                connection.disconnect();
                throw new Exception(String.format("error='bad response code %d'", resCode));
            }
        }
        catch (Exception e) {
            logger.info(String.format("link broken uuid='%s', url='%s', error='%s'",
                    this.uuid, theUrl, e.getMessage()));
            check = false;
        }
        finally {
            logger.info(String.format("link uuid='%s', url='%s', took '%s' seconds",
                    this.uuid, theUrl, (System.currentTimeMillis() - start) / 1000));
        }
        return check;
    }

    @Override
    public boolean check() {

        InputStream is = null;
        boolean check = true;
        long start = System.currentTimeMillis();

        try {
            HttpURLConnection connection = getConnection(this.url);

            if (connection.getResponseCode() != 200) {
                logger.info(String.format("link broken uuid='%s', url='%s', error='bad response code %d'",
                        this.uuid, this.url, connection.getResponseCode()));
                return false;
            }

            is = connection.getInputStream();
            Document doc = parseXML(is);
            NodeList descNodes = doc.getElementsByTagName("name");

            if (descNodes.getLength() == 0) {
                throw new Exception(String.format("Filter count is zero uuid='%s' URL= %s",this.uuid, this.url));
            }

            for (int i = 0; i < descNodes.getLength(); i++) {
                String property = descNodes.item(i).getTextContent();
                if (!checkFilterValues(getFilterValuesUrl(property))) {
                    throw new Exception(String.format("Filter '%s' not working. URL= %s",property, getFilterValuesUrl(property)));
                }
            }
        }
        catch (Exception e) {
            logger.info(String.format("link broken uuid='%s', url='%s', error='%s'",
                    this.uuid, this.url, e.getMessage()));
            check = false;
        }
        finally {
            if (is != null) {
                try {
                    is.close();
                }
                catch (IOException e) {
                    logger.info(String.format("link broken uuid='%s', url='%s', error='%s'",
                            this.uuid, this.url, e.getMessage()));
                    check = false;
                }
            }
            logger.info(String.format("link uuid='%s', url='%s', took '%s' seconds",
                    this.uuid, this.url, (System.currentTimeMillis() - start) / 1000));

        }
        return check;
    }
}
