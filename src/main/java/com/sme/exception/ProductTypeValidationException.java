package com.sme.exception;

public class ProductTypeValidationException extends SMEException {

    public ProductTypeValidationException(String message) {
        super("PT-004", message);
    }

    public ProductTypeValidationException(String message, Throwable cause) {
        super("PT-004", message, cause);
    }
}