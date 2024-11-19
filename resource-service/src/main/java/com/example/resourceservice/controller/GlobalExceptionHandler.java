package com.example.resourceservice.controller;

import com.example.resourceservice.exception.InvalidDataException;
import com.example.resourceservice.exception.NotFoundException;
import com.example.resourceservice.model.ErrorResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

@RestControllerAdvice(basePackageClasses = {ResourceRestController.class})
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({InvalidDataException.class})
    private ResponseEntity<Object> handleInvalidDataException(final InvalidDataException invalidDataException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Objects.nonNull(invalidDataException.getErrorResponse()) ? invalidDataException.getErrorResponse() : invalidDataException.getSimpleErrorResponse());
    }

    @ExceptionHandler({NumberFormatException.class})
    private ResponseEntity<Object> handleNumberFormatException(final NumberFormatException numberFormatException) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(numberFormatException.getMessage());
    }

    @ExceptionHandler({NotFoundException.class})
    private ResponseEntity<Object> handleNotFoundException(final NotFoundException notFoundException) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Objects.nonNull(notFoundException.getErrorResponse()) ? notFoundException.getErrorResponse() : notFoundException.getSimpleErrorResponse());
    }

//    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
//    private ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupportedException(final HttpMediaTypeNotSupportedException ex) {
//        ErrorResponse errorResponse = new ErrorResponse();
//        errorResponse.setErrorMessage("Invalid file format: " + ex.getContentType() + ". Only MP3 files are allowed");
//        errorResponse.setErrorCode("400");
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
//    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(final HttpMediaTypeNotSupportedException ex, final HttpHeaders headers, final HttpStatusCode status, final WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorMessage("Invalid file format: " + ex.getContentType() + ". Only MP3 files are allowed");
        errorResponse.setErrorCode("400");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(final Exception ex, final Object body, final HttpHeaders headers, final HttpStatusCode statusCode, final WebRequest request) {
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }
}
