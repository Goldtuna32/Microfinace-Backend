package com.sme.exception;

public class CollateralTypeCreationException extends SMEException {

  public CollateralTypeCreationException(String message) {
    super("COLT-002", message);
  }

  public CollateralTypeCreationException(String message, Throwable cause) {
    super("COLT-002", message, cause);
  }
}