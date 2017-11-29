package org.fao.geonet.monitor.onlineresource;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import org.w3c.dom.Document;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import java.io.InputStream;
import java.io.StringWriter;
import java.io.PrintWriter;

import java.net.HttpURLConnection;
import java.net.URL;

public class OnlineResourceCheckerUtils {
    public static String URL_XPATH = "gmd:linkage";
    public static String NAME_XPATH = "gmd:name";
    private static Logger logger = Logger.getLogger(OnlineResourceCheckerUtils.class);

    public static CheckResult checkHttpUrl(String uuid, String url) {
        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setReadTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

            logger.debug(String.format("%s -> %d", url, connection.getResponseCode()));
            if (connection.getResponseCode() != 200) {
                String errorMessage = String.format("link broken uuid='%s', url='%s', error='bad response code %d'", uuid, url, connection.getResponseCode());
                logger.info(errorMessage);
                return new CheckResult(false, errorMessage);
            } else {
                return new CheckResult(true, null);
            }
        } catch (Exception e) {
            String errorMessage = String.format("link broken uuid='%s', url='%s', error='%s', stack='%s'", uuid, url, e.getMessage(), exceptionToString(e));
            logger.info(errorMessage);
            return new CheckResult(false, errorMessage);
        }
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

    public static String exceptionToString(Exception e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println();
        e.printStackTrace(pw);
        return sw.toString();
    }

    public static Document parseXML(InputStream stream)
            throws Exception
    {
        DocumentBuilderFactory objDocumentBuilderFactory = null;
        DocumentBuilder objDocumentBuilder = null;
        Document doc = null;
        try
        {
            objDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
            objDocumentBuilder = objDocumentBuilderFactory.newDocumentBuilder();

            doc = objDocumentBuilder.parse(stream);
        }
        catch(Exception e)
        {
            throw e;
        }

        return doc;
    }
}
