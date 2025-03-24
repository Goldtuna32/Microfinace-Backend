package com.sme.exception;

public class TransactionNotFoundException extends SMEException {

    public TransactionNotFoundException(Long id) {
        super("TXN-001", "Transaction not found with id: " + id);
    }

    public TransactionNotFoundException(String message) {
        super("TXN-001", message);
    }
}