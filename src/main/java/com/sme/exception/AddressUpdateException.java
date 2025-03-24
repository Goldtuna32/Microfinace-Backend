package com.sme.exception;

public class AddressUpdateException extends SMEException {

  public AddressUpdateException(String message) {
    super("ADDR-003", message);
  }

  public AddressUpdateException(String message, Throwable cause) {
    super("ADDR-003", message, cause);
  }
}