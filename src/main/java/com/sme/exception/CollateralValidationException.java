package com.sme.exception;

public class CollateralValidationException extends SMEException {

    public CollateralValidationException(String message) {
        super("COL-006", message);
    }

    public CollateralValidationException(String message, Throwable cause) {
        super("COL-006", message, cause);
    }
}