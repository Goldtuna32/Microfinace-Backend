package com.sme.exception;

import java.math.BigDecimal;

public class InvalidLoanAmountException extends LoanValidationException {

    public InvalidLoanAmountException(String message) {
        super(message);
    }
}