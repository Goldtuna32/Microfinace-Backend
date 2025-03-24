package com.sme.exception;

public class DealerValidationException extends SMEException {

    public DealerValidationException(String message) {
        super("DLR-005", message);
    }

    public DealerValidationException(String message, Throwable cause) {
        super("DLR-005", message, cause);
    }
}