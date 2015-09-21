package org.fao.geonet.monitor.link;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.net.HttpURLConnection;
import java.net.URL;

public class LinkCheckerUtils {
    public static String URL_XPATH = "gmd:linkage";
    public static String NAME_XPATH = "gmd:name";

    public static boolean checkHttpUrl(String url) {
        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(LinkMonitorService.timeout * 1000);
            connection.setReadTimeout(LinkMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

            LinkMonitorService.getLogger().debug(String.format("%s -> %d", url, connection.getResponseCode()));
            if (connection.getResponseCode() != 200)
                LinkMonitorService.getLogger().debug(String.format("URL '%s' is unavailable", url, connection.getResponseCode()));

            return 200 == connection.getResponseCode();
        } catch (Exception e) {
            LinkMonitorService.getLogger().debug(e.getStackTrace().toString());
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
            LinkMonitorService.getLogger().error(String.format("Error parsing onlineResource, cannot extract '%s'", path));
            LinkMonitorService.getLogger().error(onlineResource.getContent().toString());
            e.printStackTrace();
        }

        return null;
    }
}
