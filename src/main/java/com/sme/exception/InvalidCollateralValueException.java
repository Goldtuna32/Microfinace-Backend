package com.sme.exception;

import java.math.BigDecimal;

public class InvalidCollateralValueException extends CollateralValidationException {

    public InvalidCollateralValueException(BigDecimal value) {
        super("Invalid collateral value: " + value + " (must be positive)");
    }
}