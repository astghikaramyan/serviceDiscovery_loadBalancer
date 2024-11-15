package com.example.songservice.exception;

import com.example.songservice.model.ValidationErrorResponse;
import com.example.songservice.model.ErrorResponse;

public class InvalidDataException extends RuntimeException{
    private ValidationErrorResponse validationErrorResponse;
    private ErrorResponse errorResponse;
    public InvalidDataException(final String message) {
        super(message);
    }
    public InvalidDataException(final ValidationErrorResponse validationErrorResponse) {
        this.validationErrorResponse = validationErrorResponse;
    }

    public InvalidDataException(final ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
    public InvalidDataException(final String message, final ValidationErrorResponse validationErrorResponse) {
        super(message);
        this.validationErrorResponse = validationErrorResponse;
    }

    public ValidationErrorResponse getErrorResponse() {
        return validationErrorResponse;
    }

    public void setErrorResponse(final ValidationErrorResponse validationErrorResponse) {
        this.validationErrorResponse = validationErrorResponse;
    }

    public ErrorResponse getSimpleErrorResponse() {
        return errorResponse;
    }

    public void setSimpleErrorResponse(final ErrorResponse errorResponse) {
        this.errorResponse = errorResponse;
    }
}
