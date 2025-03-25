package com.sme.exception;

public class InvalidStatusTransitionException extends LoanValidationException {

    public InvalidStatusTransitionException(Integer currentStatus, Integer targetStatus) {
        super("Cannot transition loan from status " + currentStatus + " to " + targetStatus);
    }
}