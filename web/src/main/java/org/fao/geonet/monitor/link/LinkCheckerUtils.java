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

    public static boolean checkHttpUrl(String url) {
        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(LinkMonitorService.timeout * 1000);
            connection.setReadTimeout(LinkMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

/*
            // TODO IMPORTANT - who is closing these connetions...

            // "application/vnd.ogc.wms_xml"
            //connection.setInstanceFollowRedirects(true);
            logger.debug(String.format("%s -> %d", url, connection.getResponseCode()));
*/

/*
            if (connection.getResponseCode() != 200)
            // logger.info("WHOOT " + connection.getHeaderField("Content-Type") );
            String contentType = connection.getContentType();
            //http://geoserver-123.aodn.org.au/geoserver/wms?service=wms&version=1.1.1&request=GetCapabilities
            logger.info("WHOOT " + contentType );
            // if(connection.getResponseCode() = 200)  check if this compiles,
*/
            if(connection.getResponseCode() != 200) {
                logger.info(String.format("URL '%s' is unavailable, response was -> %d", url, connection.getResponseCode()));
                return false;
            }

            logger.info("WHOOT content type is " + connection.getContentType() + " " + url);

       
            if(connection.getContentType().equals("application/vnd.ogc.wms_xml")) {

                logger.info("WHOOT got xml type - will try parsing the document");
                try { 
                    parseXML(connection.getInputStream());
                    logger.info(String.format("Succeeded parse xml"));
                    return true;
                } catch (Exception e) {
                    logger.info(String.format("Failed to parse xml %s", e));
                    return false;
                }
            }

            // return 200 == connection.getResponseCode();
        } catch (Exception e) {
            logger.info(String.format("Error checking link '%s'", url));
            logger.debug(e);
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
