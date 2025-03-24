package com.sme.exception;

import java.math.BigDecimal;

public class InvalidInterestRateException extends LoanValidationException {

    public InvalidInterestRateException(BigDecimal rate) {
        super("Invalid interest rate: " + rate + " (must be non-negative)");
    }
}