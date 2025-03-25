package com.sme.exception;

public class DealerUpdateException extends SMEException {

    public DealerUpdateException(String message) {
        super("DLR-003", message);
    }

    public DealerUpdateException(String message, Throwable cause) {
        super("DLR-003", message, cause);
    }
}