package com.sme.exception;

public class LoanCreationException extends SMEException {

  public LoanCreationException(String message) {
    super("LOAN-002", message);
  }

  public LoanCreationException(String message, Throwable cause) {
    super("LOAN-002", message, cause);
  }
}