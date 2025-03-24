package com.sme.exception;

public class CurrentAccountUpdateException extends SMEException {

    public CurrentAccountUpdateException(String message) {
        super("CA-004", message);
    }

    public CurrentAccountUpdateException(String message, Throwable cause) {
        super("CA-004", message, cause);
    }
}