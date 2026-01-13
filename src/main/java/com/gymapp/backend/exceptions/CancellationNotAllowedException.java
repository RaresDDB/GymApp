package com.gymapp.backend.exceptions;

import org.springframework.http.HttpStatus;

public class CancellationNotAllowedException extends GymException {
    private static final String ERROR_CODE = "CANCELLATION_NOT_ALLOWED";

    public CancellationNotAllowedException(String message) {
        super(message, ERROR_CODE, HttpStatus.BAD_REQUEST);
    }

    public CancellationNotAllowedException() {
        super(
                "Cancellation is not allowed within 24 hours of the scheduled time",
                ERROR_CODE,
                HttpStatus.BAD_REQUEST
        );
    }
}
