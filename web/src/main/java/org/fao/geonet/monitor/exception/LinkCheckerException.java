package org.fao.geonet.monitor.exception;

public class LinkCheckerException extends RuntimeException {

    public LinkCheckerException(String message) {
        super(message);
    }

    public LinkCheckerException(String message, Throwable cause) {
        super(message, cause);
    }
}
