package com.sme.exception;

public class InvalidBranchCodeException extends SMEException {

    public InvalidBranchCodeException(String branchCode) {
        super("BRCH-002", "Invalid branch code format: " + branchCode);
    }

    public InvalidBranchCodeException(String message, Throwable cause) {
        super("BRCH-002", message, cause);
    }
}