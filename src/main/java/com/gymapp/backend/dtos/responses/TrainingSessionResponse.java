package com.gymapp.backend.dtos.responses;

import com.gymapp.backend.enums.SessionStatus;
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
public class TrainingSessionResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private UUID trainerId;
    private String trainerName;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private SessionStatus status;
    private String notes;
    private LocalDateTime createdAt;
}
