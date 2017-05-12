package org.fao.geonet.monitor.link;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.net.HttpURLConnection;
import java.net.URL;

public class LinkCheckerUtils {
    public static String URL_XPATH = "gmd:linkage";
    public static String NAME_XPATH = "gmd:name";
    private static Logger logger = Logger.getLogger(LinkCheckerUtils.class);

    public static boolean checkHttpUrl(String url) {
        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(LinkMonitorService.timeout * 1000);
            connection.setReadTimeout(LinkMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

            logger.debug(String.format("%s -> %d", url, connection.getResponseCode()));
            if (connection.getResponseCode() != 200)
                logger.info(String.format("URL '%s' is unavailable, response was -> %d", url, connection.getResponseCode()));

            return 200 == connection.getResponseCode();
        } catch (Exception e) {
            logger.info(String.format("Error checking link '%s' reason '%s'", url, e));
        }
        return false;
    }

    public static String parseOnlineResource(Element onlineResource, String path) {
        try {
            XPath pPath = XPath.newInstance(path);
            Element element = (Element) pPath.selectSingleNode(onlineResource);
            Element firstChildElement = (Element) element.getChildren().get(0);
            return firstChildElement.getText().trim();
        } catch (JDOMException e) {
            logger.error(String.format("Error parsing onlineResource, cannot extract '%s'", path));
            logger.error(onlineResource.getContent().toString());
            logger.error(e);
        }

        return null;
    }
}
