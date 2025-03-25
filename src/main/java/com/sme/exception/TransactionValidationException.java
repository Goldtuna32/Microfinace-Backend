package com.sme.exception;

public class TransactionValidationException extends SMEException {

    public TransactionValidationException(String message) {
        super("TXN-003", message);
    }

    public TransactionValidationException(String message, Throwable cause) {
        super("TXN-003", message, cause);
    }
}