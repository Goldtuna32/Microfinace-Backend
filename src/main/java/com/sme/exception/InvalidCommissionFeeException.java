package com.sme.exception;

import java.math.BigDecimal;

public class InvalidCommissionFeeException extends HpProductValidationException {

    public InvalidCommissionFeeException(BigDecimal commissionFee) {
        super("Invalid commission fee value: " + commissionFee + " (must be non-negative)");
    }
}