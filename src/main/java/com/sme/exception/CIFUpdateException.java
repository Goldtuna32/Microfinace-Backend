package com.sme.exception;

public class CIFUpdateException extends SMEException {

    public CIFUpdateException(String message) {
        super("CIF-004", message);
    }

    public CIFUpdateException(String message, Throwable cause) {
        super("CIF-004", message, cause);
    }
}