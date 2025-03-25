package com.sme.exception;

public class LoanNotFoundException extends SMEException {

    public LoanNotFoundException(Long id) {
        super("LOAN-001", "Loan not found with id: " + id);
    }

    public LoanNotFoundException(String message) {
        super("LOAN-001", message);
    }
}