package com.sme.exception;

public class CollateralNotFoundException extends SMEException {

    public CollateralNotFoundException(Long id) {
        super("COL-001", "Collateral not found with id: " + id);
    }

    public CollateralNotFoundException(String message) {
        super("COL-001", message);
    }
}