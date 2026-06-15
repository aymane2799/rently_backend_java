package com.rently.rently.shared;

import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFound(EntityNotFoundException e) {
        return ErrorResponse.of(e.getMessage(), HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(EntityExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflict(EntityExistsException e) {
        return ErrorResponse.of(e.getMessage(), HttpStatus.CONFLICT.value());
    }

    @ExceptionHandler(QuotaExceededException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleQuotaExceeded(QuotaExceededException e) {
        return ErrorResponse.of(e.getMessage(), HttpStatus.FORBIDDEN.value());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ErrorResponse.of(message, HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleBadRequest(IllegalArgumentException e) {
        return ErrorResponse.of(e.getMessage(), HttpStatus.BAD_REQUEST.value());
    }

    @ExceptionHandler(IllegalStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIllegalState(IllegalStateException e) {
        return ErrorResponse.of(e.getMessage(), HttpStatus.CONFLICT.value());
    }
}
