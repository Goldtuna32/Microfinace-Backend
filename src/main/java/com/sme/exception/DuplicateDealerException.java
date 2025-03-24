package com.sme.exception;

public class DuplicateDealerException extends DealerValidationException {

    public DuplicateDealerException(String companyName) {
        super("Duplicate dealer found for company name: " + companyName);
    }
}