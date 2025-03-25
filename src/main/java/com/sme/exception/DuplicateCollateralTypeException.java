package com.sme.exception;

public class DuplicateCollateralTypeException extends CollateralTypeValidationException {

    public DuplicateCollateralTypeException(String name) {
        super("Duplicate collateral type found for name: " + name);
    }
}