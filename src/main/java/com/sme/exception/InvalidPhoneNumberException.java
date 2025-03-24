package com.sme.exception;

public class InvalidPhoneNumberException extends DealerValidationException {

    public InvalidPhoneNumberException(String phoneNumber) {
        super("Invalid phone number format: " + phoneNumber);
    }
}