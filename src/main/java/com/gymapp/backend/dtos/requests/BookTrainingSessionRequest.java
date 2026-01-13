package com.gymapp.backend.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookTrainingSessionRequest {
    @NotNull(message = "Member ID is required")
    private UUID memberId;

    @NotNull(message = "Trainer ID is required")
    private UUID trainerId;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledAt;

    @Min(value = 30, message = "Session duration must be at least 30 minutes")
    @Max(value = 180, message = "Session duration cannot exceed 180 minutes")
    @Builder.Default
    private Integer durationMinutes = 60;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}
