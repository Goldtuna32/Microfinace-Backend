package com.sme.exception;

public class DuplicateCollateralException extends CollateralValidationException {

    public DuplicateCollateralException(String identifier) {
        super("Duplicate collateral found for identifier: " + identifier);
    }
}