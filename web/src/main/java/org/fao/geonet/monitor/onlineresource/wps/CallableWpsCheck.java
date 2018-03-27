package org.fao.geonet.monitor.onlineresource.wps;

import net.opengis.ows.v_1_1_0.ExceptionReport;
import net.opengis.ows.v_1_1_0.ExceptionType;
import net.opengis.wps.v_1_0_0.ExecuteResponse;
import net.opengis.wps.v_1_0_0.StatusType;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;
import org.fao.geonet.monitor.onlineresource.CheckResult;
import org.fao.geonet.monitor.onlineresource.CheckResultEnum;
import org.fao.geonet.monitor.onlineresource.OnlineResourceCheckerUtils;
import org.fao.geonet.monitor.onlineresource.OnlineResourceMonitorService;
import org.joda.time.DateTime;
import org.joda.time.Seconds;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;

import static org.apache.commons.httpclient.HttpStatus.SC_OK;


public class CallableWpsCheck implements Callable<CheckResult> {

    private final Logger logger = Logger.getLogger(CallableWpsCheck.class);
    private String url;
    private String uuid;
    private String layer;


    CallableWpsCheck(String url, String uuid, String layer) {
        this.url = url;
        this.uuid = uuid;
        this.layer = layer;
    }

    public CheckResult call() throws Exception {
        long start = System.currentTimeMillis();

        try {
            String requestXml = getRequestXml();
            int timeoutSeconds = OnlineResourceMonitorService.timeoutSecondsWps;
            int pollIntervalSeconds = OnlineResourceMonitorService.pollIntervalSecondsWps;

            CheckResult result = submitAndWaitToComplete(url, requestXml, timeoutSeconds, pollIntervalSeconds);

            if(result != null) {
                logger.info("WPS CHECK RESULT [" + result.getResult().toString() + "], REASON [" + result.getResultReason() + "]");
                return result;
            } else {
                logger.error("NULL WPS check result returned!");
                return new CheckResult(CheckResultEnum.FAIL, "Unable to determine check result.");
            }
        } catch (Exception e) {
            String errorMessage = String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                    uuid, url, e.getMessage(), OnlineResourceCheckerUtils.exceptionToString(e));
            logger.info(errorMessage);
            return new CheckResult(CheckResultEnum.FAIL, errorMessage);
        } finally {
            logger.info(String.format("link uuid='%s', url='%s', took '%s' seconds",
                    uuid, url, (System.currentTimeMillis() - start) / 1000));
        }
    }

    private String getRequestXml() {

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
                "      <ows:Identifier>TestMode</ows:Identifier>\n" +
                "      <wps:Data>\n" +
                "        <wps:LiteralData>true</wps:LiteralData>\n" +
                "      </wps:Data>\n" +
                "    </wps:Input>\n" +
                "    <wps:Input>\n" +
                "      <ows:Identifier>subset</ows:Identifier>\n" +
                "      <wps:Data>\n" +
                "        <wps:LiteralData>LATITUDE,-31.6855,-31.6855;LONGITUDE,114.8291,114.8291</wps:LiteralData>\n" +
                "      </wps:Data>\n" +
                "    </wps:Input>\n" +
                "  </wps:DataInputs>\n" +
                "  <wps:ResponseForm>\n" +
                "    <wps:ResponseDocument storeExecuteResponse=\"true\" status=\"true\">" +
                "      <wps:Output asReference=\"true\" mimeType=\"text/csv\">" +
                "        <ows:Identifier>result</ows:Identifier>" +
                "      </wps:Output>" +
                "    </wps:ResponseDocument>" +
                "  </wps:ResponseForm>\n" +
                "</wps:Execute>";
    }


    private CheckResult submitAndWaitToComplete(String url, String requestXml, int timeoutSeconds, int pollIntervalSeconds) {

        try {
            DateTime start = new DateTime();
            DateTime now;

            String statusUrl = submitHttpPost(url, requestXml);
            logger.info("Submitted request to [" + url + "].  Status URL [" + statusUrl + "]");

            System.out.println("Waiting for process to complete...");

            boolean completed  = false;
            //  Poll the status URL & wait for the document to contain a
            //  ExceptionReport or an ExecuteResponse
            while(completed == false) {

                //  Calculate elapsed seconds
                DateTime loopStart = new DateTime();
                Seconds elapsedSeconds = Seconds.secondsBetween(start, loopStart);
                logger.info("Seconds elapsed waiting: " + elapsedSeconds.getSeconds());
                if(elapsedSeconds.getSeconds() > timeoutSeconds) {
                    logger.error("Process execution time [" + elapsedSeconds.getSeconds() + "s] has exceeded the timeout [" + timeoutSeconds + "s]");
                    completed = true;
                    return new CheckResult(CheckResultEnum.FAIL, "Process execution timed out after [" + elapsedSeconds.getSeconds() + " seconds]");
                }


                //  Read status document and determine whether the process has
                //  completed.
                ExecuteResponse response = getExecuteResponse(statusUrl);

                if(response != null) {
                    if (response.isSetStatus()) {
                        StatusType responseStatus = response.getStatus();
                        if (responseStatus.isSetProcessSucceeded()) {
                            logger.info("  -  Process status : SUCCEEDED");
                            completed = true;
                            return new CheckResult(CheckResultEnum.SUCCESS, "Process execution successful.");
                        }

                        if (responseStatus.isSetProcessAccepted()) {
                            logger.info("  -  Process status : ACCEPTED");
                        }

                        if (responseStatus.isSetProcessPaused()) {
                            logger.info("  -  Process status : PAUSED");
                        }

                        if (responseStatus.isSetProcessFailed()) {
                            logger.info("  -  Process status : FAILED");
                            completed = true;
                            ExceptionReport exceptionReport = responseStatus.getProcessFailed().getExceptionReport();
                            List<ExceptionType> exceptions = exceptionReport.getException();
                            StringBuilder exceptionStringBuilder = new StringBuilder();
                            for (ExceptionType exception : exceptions) {
                                exceptionStringBuilder.append("  -  Process exception : Code [" + exception.getExceptionCode() + "], Text [" + exception.getExceptionText() + "], Locator [" + exception.getLocator() + "]\n");
                            }
                            logger.error("  Exception in process: " + exceptionStringBuilder.toString());
                            return new CheckResult(CheckResultEnum.FAIL, exceptionStringBuilder.toString());
                        }
                    } else {
                        logger.info("  -  Process status not set.");
                    }
                } else {
                    return new CheckResult(CheckResultEnum.FAIL, "Null execute response returned.");
                }

                //  Calculate sleep interval (to try and make it a regular poll interval)
                now = new DateTime();
                elapsedSeconds = Seconds.secondsBetween(loopStart, now);
                int adjustedSleepSeconds = pollIntervalSeconds - elapsedSeconds.getSeconds();
                logger.info("Sleeping for [" + adjustedSleepSeconds + "]...");
                Thread.sleep(adjustedSleepSeconds * 1000);
            }
        } catch(Exception ex) {
            return new CheckResult(CheckResultEnum.FAIL, "Exception: " + ex.getMessage());
        }

        return null;
    }


    private String submitHttpPost(String postUrl, String body) throws IOException, JAXBException {
        HttpClient httpClient = new HttpClient();
        PostMethod postMethod = new PostMethod(postUrl);
        StringRequestEntity bodyEntity = new StringRequestEntity(body, "text/xml", "ISO-8859-1");
        postMethod.setRequestEntity(bodyEntity);

        int responseCode = httpClient.executeMethod(postMethod);

        if(responseCode != SC_OK) {
            logger.error("HTTP POST to [" + postUrl + "] unsuccessful.  Response code: " + responseCode);
            throw new HttpException("HTTP POST to [" + postUrl + "] unsuccessful.  Response code: " + responseCode);
        } else {
            logger.info("HTTP POST to [" + postUrl + "] successful.");
        }

        //  Unmarshal the XML response + return the status location
        JAXBContext jc = JAXBContext.newInstance(ExecuteResponse.class);
        InputStream responseXml = postMethod.getResponseBodyAsStream();
        ExecuteResponse response = (ExecuteResponse) jc.createUnmarshaller().unmarshal(responseXml);
        return response.getStatusLocation();
    }


    private ExecuteResponse getExecuteResponse(String statusUrl) throws Exception {

        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(ExecuteResponse.class);
            URL url = new URL(statusUrl);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            return (ExecuteResponse) jaxbUnmarshaller.unmarshal(url);

        } catch(Exception ex) {
            logger.error("Unable to unmarshall execute response: " + ex.getMessage(), ex);
            throw ex;
        }
    }
}
