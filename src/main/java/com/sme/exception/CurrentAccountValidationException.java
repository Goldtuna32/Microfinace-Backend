package com.sme.exception;

public class CurrentAccountValidationException extends SMEException {

    public CurrentAccountValidationException(String message) {
        super("CA-005", message);
    }

    public CurrentAccountValidationException(String message, Throwable cause) {
        super("CA-005", message, cause);
    }
}