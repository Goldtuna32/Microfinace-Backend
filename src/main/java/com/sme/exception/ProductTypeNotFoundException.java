package com.sme.exception;

public class ProductTypeNotFoundException extends SMEException {

    public ProductTypeNotFoundException(Long id) {
        super("PT-001", "Product type not found with id: " + id);
    }

    public ProductTypeNotFoundException(String message) {
        super("PT-001", message);
    }
}