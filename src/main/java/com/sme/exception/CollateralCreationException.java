package com.sme.exception;

public class CollateralCreationException extends SMEException {

    public CollateralCreationException(String message) {
        super("COL-003", message);
    }

    public CollateralCreationException(String message, Throwable cause) {
        super("COL-003", message, cause);
    }
}