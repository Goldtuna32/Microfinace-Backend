package com.sme.exception;

public class InvalidTransactionTypeException extends TransactionValidationException {

    public InvalidTransactionTypeException(String type) {
        super("Invalid transaction type: " + type + " (must be CREDIT or DEBIT)");
    }
}