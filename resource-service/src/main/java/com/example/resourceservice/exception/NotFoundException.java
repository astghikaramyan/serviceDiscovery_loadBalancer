package com.example.resourceservice.exception;

public class NotFoundException extends RuntimeException {
    public NotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(final String message) {
        super(message);
    }
}
