package com.sme.exception;

public class DuplicateCIFException extends CIFValidationException {

    public DuplicateCIFException(String identifier) {
        super("Duplicate CIF found for identifier: " + identifier);
    }
}