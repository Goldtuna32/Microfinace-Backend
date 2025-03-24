package com.sme.exception;

public class CurrentAccountCreationException extends SMEException {

    public CurrentAccountCreationException(String message) {
        super("CA-003", message);
    }

    public CurrentAccountCreationException(String message, Throwable cause) {
        super("CA-003", message, cause);
    }
}