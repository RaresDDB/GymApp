package com.gymapp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceReportResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private long totalVisits;
    private Map<LocalDate, Long> dailyVisits;
    private List<AttendanceResponse> recentAttendance;
}
