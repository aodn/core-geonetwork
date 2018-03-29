package org.fao.geonet.monitor.onlineresource.wps;

import org.apache.log4j.Logger;
import org.fao.geonet.monitor.onlineresource.CheckResult;
import org.fao.geonet.monitor.onlineresource.CheckResultEnum;
import org.fao.geonet.monitor.onlineresource.OnlineResourceCheckerDefault;
import org.fao.geonet.monitor.onlineresource.OnlineResourceCheckerUtils;
import org.fao.geonet.monitor.onlineresource.OnlineResourceMonitorService;
import org.jdom.Element;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class OnlineResourceCheckerWps extends OnlineResourceCheckerDefault {

    private static Logger logger = Logger.getLogger(OnlineResourceCheckerWps.class);
    private String layerName;
    Future<CheckResult> resultFuture;
    LocalDateTime lastRun;
    ExecutorService executorService = Executors.newSingleThreadExecutor();
    private int minutesBetweenChecks;

    public OnlineResourceCheckerWps() {
        this.minutesBetweenChecks = OnlineResourceMonitorService.wpsMinutesBetweenChecks;
    }

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        this.layerName = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.NAME_XPATH);
    }

    @Override
    public CheckResult check() {

        //  If there is a check running and it has completed - report the result
        if(this.resultFuture != null) {
            if(this.resultFuture.isDone()) {
                try {
                    CheckResult result = this.resultFuture.get();
                    this.resultFuture = null;

                    adjustCheckRunInterval(result.getResult());

                    return result;
                } catch (Exception ex) {
                    //  Something odd has happened.
                    logError("Exception getting check result: " + ex.getMessage(), ex);

                    adjustCheckRunInterval(CheckResultEnum.FAIL);

                    return new CheckResult(CheckResultEnum.FAIL, "Unable to retrieve check result: " + ex.getMessage());
                }
            } else {
                return new CheckResult(CheckResultEnum.PENDING, null);
            }
        } else {

            //  No check is running
            runCheckIfReady();

            return new CheckResult(CheckResultEnum.PENDING, null);
        }
    }


    private void adjustCheckRunInterval(CheckResultEnum result) {
        //  After a failure we want the check to run more often - so that the layer can potentially recover faster.
        //  These WPS checks were designed to only run every 24 hours - meaning that a layer would not be rechecked
        //  after a failure for another 24 hours.  Once a failure has happened we will set the retry interval to 1 hour &
        //  if a successful check result is detected, then we'll set the interval back out to the longer interval.
        if(result == CheckResultEnum.SUCCESS) {
            if(this.minutesBetweenChecks != OnlineResourceMonitorService.wpsMinutesBetweenChecks) {
                this.minutesBetweenChecks = OnlineResourceMonitorService.wpsMinutesBetweenChecks;
                logInfo("Adjusted WPS run interval to [" + this.minutesBetweenChecks + "] minutes following a SUCCESSFUL check result.");
            }
        }

        if(result == CheckResultEnum.FAIL) {
            if(this.minutesBetweenChecks != OnlineResourceMonitorService.wpsMinutesBetweenRetriesAfterFailure) {
                this.minutesBetweenChecks = OnlineResourceMonitorService.wpsMinutesBetweenRetriesAfterFailure;
                logInfo("Adjusted WPS run interval to [" + this.minutesBetweenChecks + "] minutes following a FAILED check result.");
            }
        }
    }


    /**
     * Start a checking thread if this is the first time the check has been run or if the time since the last
     * check exceeds the configured time between checks.
     */
    private void runCheckIfReady() {

        LocalDateTime now = new LocalDateTime();
        int minutesElapsedSinceLastRun = 0;

        if(lastRun != null) {
            Minutes elapsedMinutes = Minutes.minutesBetween(this.lastRun, now);
            minutesElapsedSinceLastRun = elapsedMinutes.getMinutes();
        }

        logInfo("Minutes since last check = " + minutesElapsedSinceLastRun + ", Minutes between checks = " + minutesBetweenChecks);

        //  If the check hasn't yet been run - or the time since the last run is greater than
        //  or equal to the configured interval.
        if(this.lastRun == null || minutesElapsedSinceLastRun >= this.minutesBetweenChecks) {
            this.lastRun = new LocalDateTime();

            logInfo("Starting new check.");

            //  Run an asynchronous check.  Until the result has been determined (a separate thread will go off and run the check)
            //  the check will return PENDING as the result.
            this.resultFuture = executorService.submit(new CallableWpsCheck(this.url, this.uuid, this.layerName));
        }
    }


    private void logInfo(String message) {
        logger.info("Check WPS layer [" + this.layerName + "]: " + message);
    }

    private void logError(String message, Throwable ex) {
        logger.error("Check WPS layer [" + this.layerName + "]: " + message, ex);
    }
}
