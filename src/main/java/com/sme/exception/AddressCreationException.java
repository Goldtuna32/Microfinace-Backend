package com.sme.exception;

public class AddressCreationException extends SMEException {

    public AddressCreationException(String message) {
        super("ADDR-002", message);
    }

    public AddressCreationException(String message, Throwable cause) {
        super("ADDR-002", message, cause);
    }
}