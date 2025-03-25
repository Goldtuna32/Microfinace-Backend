package com.sme.exception;

public class HpProductCreationException extends SMEException {

    public HpProductCreationException(String message) {
        super("HPP-002", message);
    }

    public HpProductCreationException(String message, Throwable cause) {
        super("HPP-002", message, cause);
    }
}