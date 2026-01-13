package com.gymapp.backend.exceptions;

import org.springframework.http.HttpStatus;

public class ClassFullException extends GymException {
    private static final String ERROR_CODE = "CLASS_FULL";

    public ClassFullException(String className) {
        super(
                String.format("Class '%s' has reached maximum capacity", className),
                ERROR_CODE,
                HttpStatus.BAD_REQUEST
        );
    }

    public ClassFullException(String className, int maxCapacity) {
        super(
                String.format("Class '%s' is full. Maximum capacity: %d", className, maxCapacity),
                ERROR_CODE,
                HttpStatus.BAD_REQUEST
        );
    }
}
