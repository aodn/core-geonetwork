package org.fao.geonet.monitor.link;

import org.apache.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class LinkCheckerWfs extends LinkCheckerDefault {

    private static Logger logger = Logger.getLogger(LinkCheckerWfs.class);

    @Override
    public void setOnlineResource(String uuid, final org.jdom.Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        String name = LinkCheckerUtils.parseOnlineResource(onlineResource, LinkCheckerUtils.NAME_XPATH);
        url += "?service=WFS&version=1.0.0&request=GetFeature&maxFeatures=1&outputFormat=gml2&typeName=" + name;
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

            // text/xml; subtype=gml/2.1.2
            if (connection.getContentType().contains("text/xml")) {
                InputStream is = null;
                try {
                    is = connection.getInputStream();
                    Document doc = LinkCheckerUtils.parseXML(is);
                    Element e = doc.getDocumentElement();

                    if(e.getTagName().equals("wfs:FeatureCollection")
                        && ((Element)e.getFirstChild().getNextSibling()).getTagName().equals("gml:featureMember")) {
                        // ok
                        return true;
                    } else {
                        logger.info(String.format("link broken uuid='%s', url='%s', error='wfs response not recognized",
                                this.uuid, this.url ));
                        return false;
                    }
                } finally {
                    if(is != null) {
                        is.close();
                    }
                }
            } else {
                logger.info(String.format("link broken uuid='%s', url='%s', error='unexpected content-type %s'",
                        this.uuid, this.url, connection.getContentType()));
                return false;
            }

        } catch (Exception e) {

            logger.info(String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                    uuid, url, e.getMessage(), LinkCheckerUtils.exceptionToString(e)));
        }

        return false;
    }
}
