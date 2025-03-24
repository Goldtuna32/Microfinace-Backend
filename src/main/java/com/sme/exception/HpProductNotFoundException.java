package com.sme.exception;

public class HpProductNotFoundException extends SMEException {

    public HpProductNotFoundException(Long id) {
        super("HPP-001", "HP Product not found with id: " + id);
    }

    public HpProductNotFoundException(String message) {
        super("HPP-001", message);
    }
}