package com.sme.exception;

import java.time.LocalDateTime;

public abstract class SMEException extends RuntimeException {

    private final String errorCode;
    private final LocalDateTime timestamp;
    private final String detailedMessage;

    /**
     * Constructs a new SMEException with the specified message
     * @param message The error message
     */
    public SMEException(String message) {
        super(message);
        this.errorCode = "SME-000"; // Default error code
        this.timestamp = LocalDateTime.now();
        this.detailedMessage = message;
    }

    /**
     * Constructs a new SMEException with the specified message and cause
     * @param message The error message
     * @param cause The cause of the exception
     */
    public SMEException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = "SME-000"; // Default error code
        this.timestamp = LocalDateTime.now();
        this.detailedMessage = message;
    }

    /**
     * Constructs a new SMEException with specified error code and message
     * @param errorCode A unique code identifying the error type
     * @param message The error message
     */
    public SMEException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.detailedMessage = message;
    }

    /**
     * Constructs a new SMEException with specified error code, message, and cause
     * @param errorCode A unique code identifying the error type
     * @param message The error message
     * @param cause The cause of the exception
     */
    public SMEException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.detailedMessage = message;
    }

    // Getters
    public String getErrorCode() {
        return errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    // Override toString for better logging
    @Override
    public String toString() {
        return String.format(
                "SMEException{errorCode='%s', message='%s', timestamp='%s'}",
                errorCode, getMessage(), timestamp
        );
    }
}