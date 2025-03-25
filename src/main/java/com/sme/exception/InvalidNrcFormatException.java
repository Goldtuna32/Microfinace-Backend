package com.sme.exception;

public class InvalidNrcFormatException extends CIFValidationException {

    public InvalidNrcFormatException(String nrc) {
        super("Invalid NRC format: " + nrc);
    }
}