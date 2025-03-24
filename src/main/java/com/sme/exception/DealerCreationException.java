package com.sme.exception;

public class DealerCreationException extends SMEException {

    public DealerCreationException(String message) {
        super("DLR-002", message);
    }

    public DealerCreationException(String message, Throwable cause) {
        super("DLR-002", message, cause);
    }
}