package com.sme.exception;

public class DuplicateHpProductException extends HpProductValidationException {

  public DuplicateHpProductException(String name, Long dealerId) {
    super("Duplicate HP product found for name: " + name + " and dealer ID: " + dealerId);
  }
}