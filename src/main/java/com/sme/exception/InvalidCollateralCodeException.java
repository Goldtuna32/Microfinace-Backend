package com.sme.exception;

public class InvalidCollateralCodeException extends SMEException {

    public InvalidCollateralCodeException(String collateralCode) {
        super("COL-002", "Invalid collateral code format: " + collateralCode);
    }

    public InvalidCollateralCodeException(String message, Throwable cause) {
        super("COL-002", message, cause);
    }
}