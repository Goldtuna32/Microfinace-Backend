package com.sme.exception;

public class CIFValidationException extends SMEException {

    public CIFValidationException(String message) {
        super("CIF-006", message);
    }

    public CIFValidationException(String message, Throwable cause) {
        super("CIF-006", message, cause);
    }
}