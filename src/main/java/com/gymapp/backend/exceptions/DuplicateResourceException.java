package com.gymapp.backend.exceptions;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends GymException {
    private static final String ERROR_CODE = "DUPLICATE_RESOURCE";

    public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
        super(
                String.format("%s already exists with %s: '%s'", resourceName, fieldName, fieldValue),
                ERROR_CODE,
                HttpStatus.CONFLICT
        );
    }

    public DuplicateResourceException(String message) {
        super(message, ERROR_CODE, HttpStatus.CONFLICT);
    }
}
