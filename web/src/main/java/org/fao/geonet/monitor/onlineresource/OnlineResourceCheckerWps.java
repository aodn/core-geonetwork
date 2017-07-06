package org.fao.geonet.monitor.onlineresource;


import au.com.bytecode.opencsv.CSVReader;
import org.apache.log4j.Logger;
import org.jdom.Element;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

public class OnlineResourceCheckerWps extends OnlineResourceCheckerDefault {

    private static Logger logger = Logger.getLogger(OnlineResourceCheckerWps.class);
    private static String WFS_GET_FEATURES = "?service=WFS&version=1.0.0&request=GetFeature&maxFeatures=1&outputFormat=csv&typeName=";
    private String wfsUrl;
    private String layer;

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        layer = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.NAME_XPATH);
        wfsUrl = url + WFS_GET_FEATURES + layer;
        url += "?service=WPS";
    }

    @Override
    public boolean check() {
        long start = System.currentTimeMillis();
        try {
            String time = getTime();

            // If the wfs request fails or returns csv without time value
            if (time == null)
                return false;

            String requestXml = getRequestXml(time);

            HttpURLConnection connection;
            connection = (HttpURLConnection) (new URL(url)).openConnection();
            connection.setConnectTimeout(OnlineResourceMonitorService.timeout_wps * 1000);
            connection.setReadTimeout(OnlineResourceMonitorService.timeout_wps * 1000);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "text/xml");
            connection.setDoOutput(true);

            OutputStreamWriter writer = null;
            try {
                writer = new OutputStreamWriter(connection.getOutputStream());
                writer.write(requestXml);
            } catch (Exception e) {
                throw e;
            } finally {
                if(writer != null) {
                    writer.flush();
                    writer.close();
                }
            }

            logger.debug(String.format("%s -> %d", url, connection.getResponseCode()));

            if (connection.getResponseCode() != 200) {
                logger.info(String.format("link broken uuid='%s', url='%s', error='bad response code %d'",
                        this.uuid, this.url, connection.getResponseCode()));
                return false;
            }

            // Checking if file is readable
            if (connection.getContentType().equals("text/csv")) {
                InputStream is = null;
                CSVReader reader;
                try {
                    is = connection.getInputStream();
                    reader = new CSVReader(new InputStreamReader(connection.getInputStream()));
                    if (reader.readNext() == null) {
                        logger.info(String.format("link broken uuid='%s', url='%s', error='empty row data in csv file'", this.uuid, this.url));
                        return false;
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
                return true;
            } else {
                logger.info(String.format("link broken uuid='%s', url='%s', error='unexpected content-type'", this.uuid, this.url));
            }

        } catch (Exception e) {
            logger.info(String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                    uuid, url, e.getMessage(), OnlineResourceCheckerUtils.exceptionToString(e)));
        } finally {
            logger.info(String.format("link uuid='%s', url='%s', took '%s' seconds",
                    uuid, url, (System.currentTimeMillis() - start) / 1000));
        }

        return false;
    }

    private String getRequestXml(String time) {
        String literalTime = time + "00Z";
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><wps:Execute version=\"1.0.0\" service=\"WPS\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.opengis.net/wps/1.0.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wcs=\"http://www.opengis.net/wcs/1.1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd\">\n" +
                "  <ows:Identifier>gs:GoGoDuck</ows:Identifier>\n" +
                "  <wps:DataInputs>\n" +
                "    <wps:Input>\n" +
                "      <ows:Identifier>layer</ows:Identifier>\n" +
                "      <wps:Data>\n" +
                "        <wps:LiteralData>" + layer + "</wps:LiteralData>\n" +
                "      </wps:Data>\n" +
                "    </wps:Input>\n" +
                "    <wps:Input>\n" +
                "      <ows:Identifier>subset</ows:Identifier>\n" +
                "      <wps:Data>\n" +
                "        <wps:LiteralData>TIME," + literalTime + "," + literalTime + ";LATITUDE,-31.6855,-31.6855;LONGITUDE,114.8291,114.8291</wps:LiteralData>\n" +
                "      </wps:Data>\n" +
                "    </wps:Input>\n" +
                "    <wps:Input>\n" +
                "      <ows:Identifier>format</ows:Identifier>\n" +
                "      <wps:Data>\n" +
                "        <wps:LiteralData>text/csv</wps:LiteralData>\n" +
                "      </wps:Data>\n" +
                "    </wps:Input>\n" +
                "  </wps:DataInputs>\n" +
                "  <wps:ResponseForm>\n" +
                "    <wps:RawDataOutput mimeType=\"application/octet-stream\">\n" +
                "      <ows:Identifier>result</ows:Identifier>\n" +
                "    </wps:RawDataOutput>\n" +
                "  </wps:ResponseForm>\n" +
                "</wps:Execute>";
    }

    private String getTime() {
        String time = null;
        try {
            HttpURLConnection connection;
            connection = (HttpURLConnection) (new URL(wfsUrl)).openConnection();
            connection.setConnectTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setReadTimeout(OnlineResourceMonitorService.timeout * 1000);
            connection.setRequestMethod("GET");
            connection.connect();

            logger.debug(String.format("%s -> %d", wfsUrl, connection.getResponseCode()));

            if (connection.getResponseCode() != 200) {
                logger.info(String.format("link broken uuid='%s', url='%s', error='bad response code %d'",
                        this.uuid, this.wfsUrl, connection.getResponseCode()));
            }

            if (connection.getContentType().equals("text/csv;charset=UTF-8")) {
                InputStream is = null;
                CSVReader reader;
                try {
                    is = connection.getInputStream();
                    reader = new CSVReader(new InputStreamReader(connection.getInputStream()));
                    String[] row;

                    int rowCount = 1, timeColumn = -1;
                    while ((row = reader.readNext()) != null) {
                        if (rowCount == 1) {
                            // Looking for time header
                            for(int i=0; i<row.length; i++) {
                                if ("time".equals(row[i])) {
                                    timeColumn = i;
                                    break;
                                }
                            }
                        } else if (rowCount == 2 && timeColumn != -1){
                            // Reading time value
                            time = row[timeColumn];
                            break;
                        } else {
                            // We are interested in first two rows only
                            break;
                        }
                        rowCount++;
                    }
                } finally {
                    if (is != null) {
                        is.close();
                    }
                }
            } else {
                logger.info(String.format("link broken uuid='%s', url='%s', error='unexpected content-type'", this.uuid, this.wfsUrl));
            }

        } catch (Exception e) {
            logger.info(String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                    uuid, wfsUrl, e.getMessage(), OnlineResourceCheckerUtils.exceptionToString(e)));
        }

        return time;
    }
}
