package com.sme.exception;

public class InvalidCIFCodeException extends SMEException {

    public InvalidCIFCodeException(String cifCode) {
        super("CIF-002", "Invalid CIF code format: " + cifCode);
    }

    public InvalidCIFCodeException(String message, Throwable cause) {
        super("CIF-002", message, cause);
    }
}