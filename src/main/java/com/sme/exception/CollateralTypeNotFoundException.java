package com.sme.exception;

public class CollateralTypeNotFoundException extends SMEException {

  public CollateralTypeNotFoundException(Long id) {
    super("COLT-001", "Collateral type not found with id: " + id);
  }

  public CollateralTypeNotFoundException(String message) {
    super("COLT-001", message);
  }
}