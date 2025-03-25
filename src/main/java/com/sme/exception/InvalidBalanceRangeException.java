package com.sme.exception;

import java.math.BigDecimal;

public class InvalidBalanceRangeException extends CurrentAccountValidationException {

    public InvalidBalanceRangeException(BigDecimal min, BigDecimal max) {
        super("Minimum balance (" + min + ") cannot be greater than maximum balance (" + max + ")");
    }
}