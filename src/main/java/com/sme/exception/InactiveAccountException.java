package com.sme.exception;

public class InactiveAccountException extends TransactionValidationException {

    public InactiveAccountException(Long accountId) {
        super("Account with ID " + accountId + " is inactive");
    }
}