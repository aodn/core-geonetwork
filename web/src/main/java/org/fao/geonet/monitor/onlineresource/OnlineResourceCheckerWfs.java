package org.fao.geonet.monitor.onlineresource;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class OnlineResourceCheckerWfs extends OnlineResourceCheckerDefault {

    private static Logger logger = Logger.getLogger(OnlineResourceCheckerWfs.class);

    @Override
    public void setOnlineResource(String uuid, final org.jdom.Element onlineResource) {

        super.setOnlineResource(uuid, onlineResource);
        String name = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.NAME_XPATH);
        url += "?service=WFS&version=1.0.0&request=GetFeature&maxFeatures=1&outputFormat=gml2&typeName=" + name;
    }

    @Override
    public CheckResult check() {
        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setReadTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

            logger.debug(String.format("%s -> %d", url, connection.getResponseCode()));

            if (connection.getResponseCode() != 200) {
                String errorMessage = String.format("WFS GetFeature link broken for uuid='%s', url='%s', error='bad response code %d'",
                        this.uuid, this.url, connection.getResponseCode());
                logger.info(errorMessage);
                return new CheckResult(CheckResultEnum.FAIL, errorMessage);
            }

            // text/xml; subtype=gml/2.1.2
            if (connection.getContentType().contains("text/xml")) {
                InputStream is = null;
                try {
                    is = connection.getInputStream();
                    Document doc = OnlineResourceCheckerUtils.parseXML(is);
                    Element e = doc.getDocumentElement();

                    if (e.getTagName().equals("wfs:FeatureCollection")
                        && ((Element)e.getFirstChild().getNextSibling()).getTagName().equals("gml:featureMember")) {
                        // ok
                        return new CheckResult(CheckResultEnum.SUCCESS, null);
                    } else {
                        String errorMessage = String.format(
                                "WFS GetFeature request does not return a FeatureCollection for uuid='%s', url='%s', error='wfs response not recognized'",
                                this.uuid, this.url);
                        logger.info(errorMessage);
                        return new CheckResult(CheckResultEnum.FAIL, errorMessage);
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            } else {
                String errorMessage = String.format(
                        "WFS GetFeature request returns non-xml content for uuid='%s', url='%s', error='unexpected content-type %s'",
                        this.uuid, this.url, connection.getContentType());
                logger.info(errorMessage);
                return new CheckResult(CheckResultEnum.FAIL, errorMessage);
            }

        } catch (Exception e) {
            String errorMessage = String.format(
                    "WFS GetFeature request results in exception for uuid='%s', url='%s', error='%s' stack='%s'",
                    this.uuid, url, e.getMessage(), OnlineResourceCheckerUtils.exceptionToString(e));
            logger.info(errorMessage);
            return new CheckResult(CheckResultEnum.FAIL, errorMessage);
        }
    }
}
