package com.sme.exception;

import java.math.BigDecimal;

public class BalanceLimitExceededException extends TransactionValidationException {

    public BalanceLimitExceededException(BigDecimal balance, BigDecimal maximumBalance) {
        super("Transaction exceeds maximum balance limit. New balance (" + balance + ") exceeds (" + maximumBalance + ")");
    }
}