package org.fao.geonet.monitor.link;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;

import java.net.HttpURLConnection;
import java.net.URL;

import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
// import org.w3c.dom.NodeList;


public class LinkCheckerUtils {
    public static String URL_XPATH = "gmd:linkage";
    public static String NAME_XPATH = "gmd:name";
    private static Logger logger = Logger.getLogger(LinkCheckerUtils.class);


    private static Document parseXML(InputStream stream)
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
        catch(Exception ex)
        {
            throw ex;
        }       

        return doc;
    }

    public static void checkHttpUrl(String url) throws Exception {
        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(LinkMonitorService.timeout * 1000);
            connection.setReadTimeout(LinkMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

/*
            // TODO IMPORTANT - who is closing these connetions...
*/
            if(connection.getResponseCode() != 200) {
                String  s = String.format("URL '%s' bad response code, %d", url, connection.getResponseCode());
                // logger.info(String.format("URL '%s' is unavailable, response was -> %d", url, connection.getResponseCode()));
                throw new RuntimeException(s);
            }

            logger.info("WHOOT content type is " + connection.getContentType() + " " + url);

            if(connection.getContentType().equals("application/vnd.ogc.wms_xml")) {

                logger.info("WHOOT got xml type - will try parsing the document");
                parseXML(connection.getInputStream());
                logger.info(String.format("WHOOT Succeeded parse xml"));
                return;
            }

        } catch (Exception e) {
            logger.info(String.format("Error checking link '%s' reason '%s'", url, e));
//            logger.info(String.format("Error checking link '%s'", url));
//            logger.debug(e);
            throw e;
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
}
