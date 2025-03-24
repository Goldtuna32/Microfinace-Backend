package com.sme.exception;

public class DealerNotFoundException extends SMEException {

    public DealerNotFoundException(Long id) {
        super("DLR-001", "Dealer not found with id: " + id);
    }

    public DealerNotFoundException(String message) {
        super("DLR-001", message);
    }
}