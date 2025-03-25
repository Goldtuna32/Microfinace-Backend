package com.sme.exception;

public class CollateralTypeValidationException extends SMEException {

    public CollateralTypeValidationException(String message) {
        super("COLT-004", message);
    }

    public CollateralTypeValidationException(String message, Throwable cause) {
        super("COLT-004", message, cause);
    }
}