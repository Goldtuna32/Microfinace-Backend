package com.sme.exception;

public class ProductTypeCreationException extends SMEException {

    public ProductTypeCreationException(String message) {
        super("PT-002", message);
    }

    public ProductTypeCreationException(String message, Throwable cause) {
        super("PT-002", message, cause);
    }
}