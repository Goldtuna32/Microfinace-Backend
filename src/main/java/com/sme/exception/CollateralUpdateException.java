package com.sme.exception;

public class CollateralUpdateException extends SMEException {

    public CollateralUpdateException(String message) {
        super("COL-004", message);
    }

    public CollateralUpdateException(String message, Throwable cause) {
        super("COL-004", message, cause);
    }
}