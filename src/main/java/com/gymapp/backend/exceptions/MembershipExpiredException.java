package com.gymapp.backend.exceptions;

import org.springframework.http.HttpStatus;

public class MembershipExpiredException extends GymException {
    private static final String ERROR_CODE = "MEMBERSHIP_EXPIRED";

    public MembershipExpiredException(String memberName) {
        super(
                String.format("Membership for '%s' has expired", memberName),
                ERROR_CODE,
                HttpStatus.FORBIDDEN
        );
    }

    public MembershipExpiredException() {
        super("Membership has expired. Please renew your subscription.", ERROR_CODE, HttpStatus.FORBIDDEN);
    }
}
