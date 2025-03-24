package com.sme.exception;

import java.math.BigDecimal;

public class InvalidPriceException extends HpProductValidationException {

    public InvalidPriceException(BigDecimal price) {
        super("Invalid price value: " + price + " (must be positive)");
    }
}