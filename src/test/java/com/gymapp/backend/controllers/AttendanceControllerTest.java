package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CheckInRequest;
import com.gymapp.backend.dtos.responses.AttendanceReportResponse;
import com.gymapp.backend.dtos.responses.AttendanceResponse;
import com.gymapp.backend.exceptions.GlobalExceptionHandler;
import com.gymapp.backend.exceptions.InvalidOperationException;
import com.gymapp.backend.exceptions.MembershipExpiredException;
import com.gymapp.backend.services.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttendanceController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureJsonTesters
class AttendanceControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<CheckInRequest> checkInRequestJson;

    @MockitoBean
    @SuppressWarnings("unused")
    private AttendanceService attendanceService;

    private UUID memberId;

    private UUID attendanceId;

    private CheckInRequest checkInRequest;

    private AttendanceResponse attendanceResponse;

    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();
        attendanceId = UUID.randomUUID();

        attendanceResponse = AttendanceResponse.builder()
            .id(attendanceId)
            .memberId(memberId)
            .memberName("John Doe")
            .checkInTime(LocalDateTime.now())
            .visitDate(LocalDate.now())
            .build();

        checkInRequest = CheckInRequest.builder()
            .memberId(memberId)
            .build();
    }

    @Test
    @DisplayName("POST /api/attendance/check-in - Should check in member successfully")
    void checkIn_Success() throws Exception {
        when(attendanceService.checkIn(any(CheckInRequest.class))).thenReturn(attendanceResponse);

        mockMvc.perform(
            post("/api/attendance/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(checkInRequestJson.write(checkInRequest).getJson())
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(attendanceId.toString()))
        .andExpect(jsonPath("$.memberName").value("John Doe"));
    }

    @Test
    @DisplayName("POST /api/attendance/check-in - Should return 403 when membership expired")
    void checkIn_MembershipExpired() throws Exception {
        when(attendanceService.checkIn(any(CheckInRequest.class)))
            .thenThrow(new MembershipExpiredException("John Doe"));

        mockMvc.perform(
            post("/api/attendance/check-in")
                .contentType(MediaType.APPLICATION_JSON)
                .content(checkInRequestJson.write(checkInRequest).getJson())
        )
        .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/attendance/check-in - Should return 400 when already checked in")
    void checkIn_AlreadyCheckedIn() throws Exception {
        when(attendanceService.checkIn(any(CheckInRequest.class)))
                .thenThrow(new InvalidOperationException("Already checked in"));

        mockMvc.perform(post("/api/attendance/check-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(checkInRequestJson.write(checkInRequest).getJson()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/attendance/{id}/check-out - Should check out successfully")
    void checkOut_Success() throws Exception {
        attendanceResponse.setCheckOutTime(LocalDateTime.now());
        attendanceResponse.setDurationMinutes(60L);
        when(attendanceService.checkOut(attendanceId)).thenReturn(attendanceResponse);

        mockMvc.perform(put("/api/attendance/{id}/check-out", attendanceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.durationMinutes").value(60));
    }

    @Test
    @DisplayName("GET /api/attendance/{id} - Should return attendance by ID")
    void getAttendanceById_Success() throws Exception {
        when(attendanceService.getAttendanceById(attendanceId)).thenReturn(attendanceResponse);

        mockMvc.perform(get("/api/attendance/{id}", attendanceId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(attendanceId.toString()));
    }

    @Test
    @DisplayName("GET /api/attendance/member/{memberId} - Should return member attendance")
    void getMemberAttendance_Success() throws Exception {
        Page<AttendanceResponse> page = new PageImpl<>(List.of(attendanceResponse));
        when(attendanceService.getMemberAttendancePaged(eq(memberId), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/attendance/member/{memberId}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].memberId").value(memberId.toString()));
    }

    @Test
    @DisplayName("GET /api/attendance/report - Should return attendance report")
    void getAttendanceReport_Success() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        Map<LocalDate, Long> dailyVisits = new HashMap<>();
        dailyVisits.put(LocalDate.now(), 10L);

        AttendanceReportResponse report = AttendanceReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalVisits(50)
                .dailyVisits(dailyVisits)
                .recentAttendance(List.of(attendanceResponse))
                .build();

        when(attendanceService.getAttendanceReport(startDate, endDate)).thenReturn(report);

        mockMvc.perform(get("/api/attendance/report")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalVisits").value(50));
    }

    @Test
    @DisplayName("GET /api/attendance/member/{memberId}/count - Should return visit count")
    void getMemberVisitCount_Success() throws Exception {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(attendanceService.getMemberVisitCount(memberId, startDate, endDate)).thenReturn(15L);

        mockMvc.perform(get("/api/attendance/member/{memberId}/count", memberId)
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(content().string("15"));
    }
}
