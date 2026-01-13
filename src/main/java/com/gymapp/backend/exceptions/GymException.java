package com.gymapp.backend.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class GymException extends RuntimeException {
    private final String errorCode;
    private final HttpStatus status;

    public GymException(String message, String errorCode, HttpStatus status) {
        super(message);
        this.errorCode = errorCode;
        this.status = status;
    }

    public GymException(String message, String errorCode, HttpStatus status, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.status = status;
    }
}
