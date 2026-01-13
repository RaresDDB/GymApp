package com.gymapp.backend.dtos.responses;

import com.gymapp.backend.enums.ClassType;
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
public class GymClassResponse {
    private UUID id;
    private String name;
    private String description;
    private String instructor;
    private Integer maxCapacity;
    private Integer currentEnrollment;
    private Integer availableSpots;
    private LocalDateTime scheduledAt;
    private Integer durationMinutes;
    private ClassType classType;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
