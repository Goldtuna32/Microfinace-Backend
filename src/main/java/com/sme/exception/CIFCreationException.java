package com.sme.exception;

public class CIFCreationException extends SMEException {

    public CIFCreationException(String message) {
        super("CIF-003", message);
    }

    public CIFCreationException(String message, Throwable cause) {
        super("CIF-003", message, cause);
    }
}