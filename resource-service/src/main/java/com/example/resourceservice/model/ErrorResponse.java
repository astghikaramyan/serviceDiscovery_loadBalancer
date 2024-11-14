package com.example.resourceservice.model;

public class ErrorResponse {
    private String errorMessage;
    private ErrorDetails errorDetails;
    private String errorCode;

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public ErrorDetails getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(final ErrorDetails errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(final String errorCode) {
        this.errorCode = errorCode;
    }
}
