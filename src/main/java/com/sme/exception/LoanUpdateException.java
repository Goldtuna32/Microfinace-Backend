package com.sme.exception;

public class LoanUpdateException extends SMEException {

    public LoanUpdateException(String message) {
        super("LOAN-003", message);
    }

    public LoanUpdateException(String message, Throwable cause) {
        super("LOAN-003", message, cause);
    }
}