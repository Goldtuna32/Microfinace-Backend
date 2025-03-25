package com.sme.exception;

public class CIFNotFoundException extends SMEException {

    public CIFNotFoundException(Long id) {
        super("CIF-001", "CIF not found with id: " + id);
    }

    public CIFNotFoundException(String message) {
        super("CIF-001", message);
    }
}