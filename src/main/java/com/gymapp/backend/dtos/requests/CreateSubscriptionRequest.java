package com.gymapp.backend.dtos.requests;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {
    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotNull(message = "Plan ID is required")
    private UUID planId;

    @FutureOrPresent(message = "Start date must be today or in the future")
    private LocalDate startDate;
}
