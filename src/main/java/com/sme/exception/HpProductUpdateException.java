package com.sme.exception;

public class HpProductUpdateException extends SMEException {

    public HpProductUpdateException(String message) {
        super("HPP-003", message);
    }

    public HpProductUpdateException(String message, Throwable cause) {
        super("HPP-003", message, cause);
    }
}