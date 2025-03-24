package com.sme.exception;

public class CurrentAccountNotFoundException extends SMEException {

    public CurrentAccountNotFoundException(Long id) {
        super("CA-001", "Current account not found with id: " + id);
    }

    public CurrentAccountNotFoundException(String message) {
        super("CA-001", message);
    }
}