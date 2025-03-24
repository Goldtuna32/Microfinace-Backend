package com.sme.exception;

public class TransactionCreationException extends SMEException {

    public TransactionCreationException(String message) {
        super("TXN-002", message);
    }

    public TransactionCreationException(String message, Throwable cause) {
        super("TXN-002", message, cause);
    }
}