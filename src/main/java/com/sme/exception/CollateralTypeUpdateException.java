package com.sme.exception;

public class CollateralTypeUpdateException extends SMEException {

    public CollateralTypeUpdateException(String message) {
        super("COLT-003", message);
    }

    public CollateralTypeUpdateException(String message, Throwable cause) {
        super("COLT-003", message, cause);
    }
}