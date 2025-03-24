package com.sme.exception;

import java.math.BigDecimal;

public class InsufficientFundsException extends TransactionValidationException {

    public InsufficientFundsException(BigDecimal balance, BigDecimal minimumBalance) {
        super("Insufficient funds. Balance (" + balance + ") below minimum required (" + minimumBalance + ")");
    }
}