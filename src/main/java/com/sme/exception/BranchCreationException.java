package com.sme.exception;

public class BranchCreationException extends SMEException {

    public BranchCreationException(String message) {
        super("BRCH-003", message);
    }

    public BranchCreationException(String message, Throwable cause) {
        super("BRCH-003", message, cause);
    }
}