package com.sme.exception;

public class DuplicateProductTypeException extends ProductTypeValidationException {

  public DuplicateProductTypeException(String name) {
    super("Duplicate product type found for name: " + name);
  }
}