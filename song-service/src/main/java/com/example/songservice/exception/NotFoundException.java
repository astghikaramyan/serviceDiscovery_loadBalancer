package com.example.songservice.exception;

import com.example.songservice.model.ErrorResponse;
import com.example.songservice.model.SimpleErrorResponse;

public class NotFoundException extends RuntimeException {
    private ErrorResponse errorResponse;
    private SimpleErrorResponse simpleErrorResponse;
    public NotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public NotFoundException(final String message) {
        super(message);
    }
    public NotFoundException(final String message, final ErrorResponse errorResponse) {
        super(message);
        this.errorResponse = errorResponse;
    }
    public NotFoundException(SimpleErrorResponse e){
        this.simpleErrorResponse = e;
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
