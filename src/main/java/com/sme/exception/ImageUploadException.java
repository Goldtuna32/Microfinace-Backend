package com.sme.exception;

public class ImageUploadException extends SMEException {

    public ImageUploadException(String message) {
        super("IMG-001", message);
    }

    public ImageUploadException(String message, Throwable cause) {
        super("IMG-001", message, cause);
    }
}