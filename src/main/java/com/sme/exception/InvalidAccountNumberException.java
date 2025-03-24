package com.sme.exception;

public class InvalidAccountNumberException extends SMEException {

    public InvalidAccountNumberException(String accountNumber) {
        super("CA-002", "Invalid account number format: " + accountNumber);
    }

    public InvalidAccountNumberException(String message, Throwable cause) {
        super("CA-002", message, cause);
    }
}