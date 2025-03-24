package com.sme.exception;

public class HpProductValidationException extends SMEException {

    public HpProductValidationException(String message) {
        super("HPP-005", message);
    }

    public HpProductValidationException(String message, Throwable cause) {
        super("HPP-005", message, cause);
    }
}