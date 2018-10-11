package org.fao.geonet.monitor.onlineresource;

import au.com.bytecode.opencsv.CSVReader;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;


public class OnlineResourceCheckerWfs extends OnlineResourceCheckerDefault {

    private static Logger logger = Logger.getLogger(OnlineResourceCheckerWfs.class);

    @Override
    public void setOnlineResource(String uuid, final org.jdom.Element onlineResource) {

        super.setOnlineResource(uuid, onlineResource);
        String name = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.NAME_XPATH);
        url += "?service=WFS&version=1.0.0&request=GetFeature&maxFeatures=1&outputFormat=csv&typeName=" + name;
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

            // text/csv
            if (connection.getContentType().contains("text/csv")) {
                InputStream is = null;

                try {
                    is = connection.getInputStream();
                    CSVReader reader = new CSVReader(new BufferedReader(new InputStreamReader(is)));
                    List<String[]> records = reader.readAll();

                    if (records.size() == 2
                        && records.get(0).length > 0
                        && records.get(0).length == records.get(1).length
                    ) {
                        return new CheckResult(CheckResultEnum.SUCCESS, null);
                    } else {
                        String errorMessage = String.format(
                                "WFS GetFeature request returns an empty or invalid csv file uuid='%s', url='%s'," +
                                    " error='wfs response not recognized'",
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
