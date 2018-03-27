package org.fao.geonet.monitor.onlineresource.wps;

import org.apache.log4j.Logger;
import org.fao.geonet.monitor.onlineresource.CheckResult;
import org.fao.geonet.monitor.onlineresource.CheckResultEnum;
import org.fao.geonet.monitor.onlineresource.OnlineResourceCheckerDefault;
import org.fao.geonet.monitor.onlineresource.OnlineResourceCheckerUtils;
import org.jdom.Element;
import org.joda.time.LocalDateTime;
import org.joda.time.Period;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class OnlineResourceCheckerWps extends OnlineResourceCheckerDefault {

    private static Logger logger = Logger.getLogger(OnlineResourceCheckerWps.class);
    private String layer;
    Future<CheckResult> resultFuture;

    //  TODO:  Make configurable
    public static final int MINUTES_BETWEEN_CHECKS = 1440;

    LocalDateTime lastRun;
    ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    public void setOnlineResource(String uuid, final Element onlineResource) {
        super.setOnlineResource(uuid, onlineResource);
        layer = OnlineResourceCheckerUtils.parseOnlineResource(onlineResource, OnlineResourceCheckerUtils.NAME_XPATH);
    }

    @Override
    public CheckResult check() {

        //  If there is a check running and it has completed - report the result
        if(resultFuture != null) {
            if(resultFuture.isDone()) {
                try {
                    CheckResult result = resultFuture.get();
                    resultFuture = null;
                    return result;
                } catch (Exception ex) {
                    //  Something odd has happened.
                    logger.error("Exception getting check result: " + ex.getMessage(), ex);
                    return new CheckResult(CheckResultEnum.FAIL, "Unable to retrieve check result: " + ex.getMessage());
                }
            } else {
                return new CheckResult(CheckResultEnum.PENDING, null);
            }
        } else {
            //  No check is running
            //  Has enough time elapsed to perform the next check?
            LocalDateTime now = new LocalDateTime();
            if(lastRun == null || new Period(lastRun, now).getMinutes() > MINUTES_BETWEEN_CHECKS) {

                lastRun = now;

                //  Run an asynchronous check.  Until the result has been determined (a separate thread will go off and run the check)
                //  the check will return PENDING as the result.
                resultFuture = executorService.submit(new CallableWpsCheck(url, uuid, layer));
            }

            return new CheckResult(CheckResultEnum.PENDING, null);
        }
    }
}
