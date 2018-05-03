package org.fao.geonet.monitor.onlineresource.wps;

import net.opengis.ows.v_1_1_0.ExceptionReport;
import net.opengis.ows.v_1_1_0.ExceptionType;
import net.opengis.wps.v_1_0_0.ExecuteResponse;
import net.opengis.wps.v_1_0_0.OutputDataType;
import net.opengis.wps.v_1_0_0.StatusType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.Callable;

import static org.apache.commons.httpclient.HttpStatus.SC_OK;


public class CallableWpsCheck implements Callable<CheckResult> {

    private final Logger logger = Logger.getLogger(CallableWpsCheck.class);
    private String url;
    private String uuid;
    private String layerName;

    public static final String EXPECTED_PROCESS_OUTPUT_NAME = "result";


    CallableWpsCheck(String url, String uuid, String layer) {
        this.url = url;
        this.uuid = uuid;
        this.layerName = layer;
    }

    public CheckResult call() throws Exception {
        long start = System.currentTimeMillis();

        try {
            String requestXml = getRequestXml();
            int timeoutSeconds = OnlineResourceMonitorService.timeoutSecondsWps;
            int pollIntervalSeconds = OnlineResourceMonitorService.pollIntervalSecondsWps;

            CheckResult result = submitAndWaitToComplete(url, requestXml, timeoutSeconds, pollIntervalSeconds);

            if(result != null) {
                logInfo("WPS CHECK RESULT [" + result.getResult().toString() + "], REASON [" + result.getResultReason() + "]");
                return result;
            } else {
                logError("NULL WPS check result returned!", null);
                return new CheckResult(CheckResultEnum.FAIL, "Unable to determine check result.");
            }
        } catch (Exception e) {
            String errorMessage = String.format("link broken uuid='%s', url='%s', error='%s' stack='%s'",
                    this.uuid, this.url, e.getMessage(), OnlineResourceCheckerUtils.exceptionToString(e));
            logger.info(errorMessage);
            return new CheckResult(CheckResultEnum.FAIL, errorMessage);
        } finally {
            logger.info(String.format("link uuid='%s', url='%s', took '%s' seconds",
                    this.uuid, this.url, (System.currentTimeMillis() - start) / 1000));
        }
    }

    private String getRequestXml() {

        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?><wps:Execute version=\"1.0.0\" service=\"WPS\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.opengis.net/wps/1.0.0\" xmlns:wfs=\"http://www.opengis.net/wfs\" xmlns:wps=\"http://www.opengis.net/wps/1.0.0\" xmlns:ows=\"http://www.opengis.net/ows/1.1\" xmlns:gml=\"http://www.opengis.net/gml\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:wcs=\"http://www.opengis.net/wcs/1.1.1\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xsi:schemaLocation=\"http://www.opengis.net/wps/1.0.0 http://schemas.opengis.net/wps/1.0.0/wpsAll.xsd\">\n" +
                "  <ows:Identifier>gs:GoGoDuck</ows:Identifier>\n" +
                "  <wps:DataInputs>\n" +
                "    <wps:Input>\n" +
                "      <ows:Identifier>layer</ows:Identifier>\n" +
                "      <wps:Data>\n" +
                "        <wps:LiteralData>" + this.layerName + "</wps:LiteralData>\n" +
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
                "        <wps:LiteralData>LATITUDE,-31.6855,-31.6855;LONGITUDE,114.8291,114.8291;DEPTH,0,100</wps:LiteralData>\n" +
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
            logInfo("Submitted request to [" + url + "].  Status URL [" + statusUrl + "]");

            boolean completed  = false;
            //  Poll the status URL & wait for the document to contain a
            //  ExceptionReport or an ExecuteResponse
            while(completed == false) {

                //  Calculate elapsed seconds
                DateTime loopStart = new DateTime();
                Seconds elapsedSeconds = Seconds.secondsBetween(start, loopStart);
                logInfo("Seconds elapsed: " + elapsedSeconds.getSeconds());
                if(elapsedSeconds.getSeconds() > timeoutSeconds) {
                    logError("Process execution time [" + elapsedSeconds.getSeconds() + "s] has exceeded the timeout [" + timeoutSeconds + "s]", null);
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
                            completed = true;

                            //  Verify that the file returned has some data in it.
                            if(verifyOutputContainsData(response)) {
                                return new CheckResult(CheckResultEnum.SUCCESS, "Process execution successful.");
                            } else {
                                return new CheckResult(CheckResultEnum.FAIL, "No data was returned from the aggregation.");
                            }
                        }

                        if (responseStatus.isSetProcessFailed()) {
                            logInfo("Process status : FAILED");
                            completed = true;
                            ExceptionReport exceptionReport = responseStatus.getProcessFailed().getExceptionReport();
                            List<ExceptionType> exceptions = exceptionReport.getException();
                            StringBuilder exceptionStringBuilder = new StringBuilder();
                            for (ExceptionType exception : exceptions) {
                                exceptionStringBuilder.append("  -  Process exception : Code [" + exception.getExceptionCode() + "], Text [" + exception.getExceptionText() + "], Locator [" + exception.getLocator() + "]\n");
                            }
                            logError("Exception in process: " + exceptionStringBuilder.toString(), null);
                            return new CheckResult(CheckResultEnum.FAIL, exceptionStringBuilder.toString());
                        }
                    } else {
                        logInfo("Process status not set.");
                    }
                } else {
                    return new CheckResult(CheckResultEnum.FAIL, "Null execute response returned.");
                }

                //  Calculate sleep interval (to try and make it a regular poll interval)
                now = new DateTime();
                elapsedSeconds = Seconds.secondsBetween(loopStart, now);
                int adjustedSleepSeconds = pollIntervalSeconds - elapsedSeconds.getSeconds();

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
            logError("HTTP POST to [" + postUrl + "] unsuccessful.  Response code: " + responseCode, null);
            throw new HttpException("HTTP POST to [" + postUrl + "] unsuccessful.  Response code: " + responseCode);
        } else {
            logInfo("HTTP POST to [" + postUrl + "] successful.");
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
            logError("Unable to unmarshall execute response: " + ex.getMessage(), ex);
            throw ex;
        }
    }


    private void logInfo(String message) {
        logger.info("Check WPS layer [" + this.layerName + "]: " + message);
    }

    private void logError(String message, Throwable ex) {
        String errorMessage = "Check WPS layer [" + this.layerName + "]: " + message;
        if(ex != null) {
            logger.error(errorMessage, ex);
        } else {
            logger.error(errorMessage);
        }
    }


    private boolean verifyOutputContainsData(ExecuteResponse response) throws IOException {
        //  Get the file referred to in the Output, read it & verify that it contains at least one
        //  row of data.

        logInfo("Checking that output contains data.");

        if(response != null && response.getProcessOutputs() != null) {
            List<OutputDataType> outputs = response.getProcessOutputs().getOutput();
            if(outputs != null && outputs.size() > 0) {
                for(OutputDataType output : outputs) {
                    if(output.getIdentifier().getValue().equalsIgnoreCase(EXPECTED_PROCESS_OUTPUT_NAME)) {

                        logInfo("Output [" + EXPECTED_PROCESS_OUTPUT_NAME + "] returned.");
                        if(output.getReference() != null && output.getReference().getHref() != null) {
                            String fileReference = output.getReference().getHref();
                            URL fileURL = new URL(fileReference);

                            try {

                                CSVParser parser = CSVParser.parse(fileURL, Charset.defaultCharset(), CSVFormat.DEFAULT);
                                List<CSVRecord> records = parser.getRecords();

                                //  We are expecting a header row and at least one data row (so 2 rows)
                                if(records.size() < 2) {
                                    logError("No data returned.  Rows in file: " + records.size(), null);
                                    return false;
                                }

                                for(CSVRecord record : records) {
                                    if(record.getRecordNumber() == 2) {
                                        logInfo("First data row: " + record.toString());
                                        return true;
                                    }
                                }

                            } catch (IOException ioex) {
                                ioex.printStackTrace();
                                logError("Unable to parse CSV output [" + fileReference + "]: " + ioex.getMessage(), ioex);
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }
}
