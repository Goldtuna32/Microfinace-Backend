package com.sme.exception;

public class LoanValidationException extends SMEException {

    public LoanValidationException(String message) {
        super("LOAN-005", message);
    }

    public LoanValidationException(String message, Throwable cause) {
        super("LOAN-005", message, cause);
    }
}