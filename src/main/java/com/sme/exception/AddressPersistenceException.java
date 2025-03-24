package com.sme.exception;

public class AddressPersistenceException extends SMEException {

    public AddressPersistenceException(String message) {
        super("ADDR-004", message);
    }

    public AddressPersistenceException(String message, Throwable cause) {
        super("ADDR-004", message, cause);
    }
}