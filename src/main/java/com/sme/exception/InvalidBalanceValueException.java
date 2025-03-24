package com.sme.exception;

import java.math.BigDecimal;

public class InvalidBalanceValueException extends CurrentAccountValidationException {

    public InvalidBalanceValueException(String field, BigDecimal value) {
        super("Invalid " + field + " value: " + value + " (must be non-negative)");
    }
}