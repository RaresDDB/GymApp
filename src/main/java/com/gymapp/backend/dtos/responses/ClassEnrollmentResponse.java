package com.gymapp.backend.dtos.responses;

import com.gymapp.backend.enums.EnrollmentStatus;
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
public class ClassEnrollmentResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private UUID gymClassId;
    private String gymClassName;
    private LocalDateTime enrolledAt;
    private EnrollmentStatus status;
}
