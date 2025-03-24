package com.sme.exception;

public class InvalidStatusException extends CollateralTypeValidationException {

    public InvalidStatusException(Integer status) {
        super("Invalid status value: " + status + " (must be 1 or 2)");
    }

    public InvalidStatusException(String message) {
        super(message);
    }
}