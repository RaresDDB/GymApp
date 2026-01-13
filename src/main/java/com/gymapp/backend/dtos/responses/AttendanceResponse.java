package com.gymapp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceResponse {
    private UUID id;
    private UUID memberId;
    private String memberName;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private LocalDate visitDate;
    private Long durationMinutes;
}
