package com.sme.exception;

public class MissingRequiredFieldException extends CIFValidationException {

    public MissingRequiredFieldException(String fieldName) {
        super("Missing required field: " + fieldName);
    }
}