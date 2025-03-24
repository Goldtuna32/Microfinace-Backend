package com.sme.exception;

public class InvalidBranchIdException extends CIFValidationException {

    public InvalidBranchIdException(Long branchId) {
        super("Invalid branch ID: " + branchId);
    }
}