package com.sme.exception;

public class AddressNotFoundException extends SMEException {

    public AddressNotFoundException(Long id) {
        super("ADDR-001", "Address not found with id: " + id);
    }

    public AddressNotFoundException(String message) {
        super("ADDR-001", message);
    }
}