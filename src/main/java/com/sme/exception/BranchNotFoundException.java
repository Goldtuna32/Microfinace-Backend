package com.sme.exception;

public class BranchNotFoundException extends SMEException {

    public BranchNotFoundException(Long id) {
        super("BRCH-001", "Branch not found with id: " + id);
    }

    public BranchNotFoundException(String message) {
        super("BRCH-001", message);
    }
}