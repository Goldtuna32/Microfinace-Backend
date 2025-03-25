package com.sme.exception;

public class ProductTypeUpdateException extends SMEException {

    public ProductTypeUpdateException(String message) {
        super("PT-003", message);
    }

    public ProductTypeUpdateException(String message, Throwable cause) {
        super("PT-003", message, cause);
    }
}