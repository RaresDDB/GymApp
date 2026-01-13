package com.gymapp.backend.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidOperationException extends GymException {
    private static final String ERROR_CODE = "INVALID_OPERATION";

    public InvalidOperationException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public InvalidOperationException(String message, Throwable cause) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST, cause);
    }
}
