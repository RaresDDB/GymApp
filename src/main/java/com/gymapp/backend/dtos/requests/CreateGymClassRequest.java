package com.gymapp.backend.dtos.requests;

import com.gymapp.backend.enums.ClassType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateGymClassRequest {
    @NotBlank(message = "Class name is required")
    @Size(min = 2, max = 100, message = "Class name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Instructor name is required")
    @Size(min = 2, max = 100, message = "Instructor name must be between 2 and 100 characters")
    private String instructor;

    @NotNull(message = "Maximum capacity is required")
    @Min(value = 1, message = "Maximum capacity must be at least 1")
    @Max(value = 100, message = "Maximum capacity cannot exceed 100")
    private Integer maxCapacity;

    @NotNull(message = "Scheduled time is required")
    @Future(message = "Scheduled time must be in the future")
    private LocalDateTime scheduledAt;

    @Min(value = 15, message = "Duration must be at least 15 minutes")
    @Max(value = 180, message = "Duration cannot exceed 180 minutes")
    @Builder.Default
    private Integer durationMinutes = 60;

    @NotNull(message = "Class type is required")
    private ClassType classType;
}
