package com.example.songservice.exception;

import com.example.songservice.model.ErrorResponse;
import com.example.songservice.model.SimpleErrorResponse;

public class InvalidDataException extends RuntimeException{
    private ErrorResponse errorResponse;
    private SimpleErrorResponse simpleErrorResponse;
    public InvalidDataException(final String message) {
        super(message);
    }
    public InvalidDataException(final ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public InvalidDataException(final SimpleErrorResponse simpleErrorResponse) {
        this.simpleErrorResponse = simpleErrorResponse;
    }
    public InvalidDataException(final String message, final ErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }

    public ErrorResponse getErrorResponse() {
        return errorResponse;
    }

    public void setErrorResponse(final ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }

    public SimpleErrorResponse getSimpleErrorResponse() {
        return simpleErrorResponse;
    }

    public void setSimpleErrorResponse(final SimpleErrorResponse simpleErrorResponse) {
        this.simpleErrorResponse = simpleErrorResponse;
    }
}
