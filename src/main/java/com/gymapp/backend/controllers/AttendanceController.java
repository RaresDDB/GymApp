package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CheckInRequest;
import com.gymapp.backend.dtos.responses.AttendanceReportResponse;
import com.gymapp.backend.dtos.responses.AttendanceResponse;
import com.gymapp.backend.services.AttendanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Tag(name = "Attendance", description = "Attendance tracking endpoints")
public class AttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/check-in")
    @Operation(summary = "Member check-in", description = "Records a member's gym entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Check-in recorded"),
            @ApiResponse(responseCode = "400", description = "Member already checked in"),
            @ApiResponse(responseCode = "403", description = "Member's membership has expired"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<AttendanceResponse> checkIn(
            @Valid @RequestBody CheckInRequest request) {
        AttendanceResponse response = attendanceService.checkIn(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/check-out")
    @Operation(summary = "Member check-out", description = "Records a member's gym exit")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Check-out recorded"),
            @ApiResponse(responseCode = "400", description = "Member already checked out"),
            @ApiResponse(responseCode = "404", description = "Attendance record not found")
    })
    public ResponseEntity<AttendanceResponse> checkOut(
            @Parameter(description = "Attendance UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.checkOut(id));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get attendance by ID", description = "Retrieves an attendance record by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attendance found"),
            @ApiResponse(responseCode = "404", description = "Attendance not found")
    })
    public ResponseEntity<AttendanceResponse> getAttendanceById(
            @Parameter(description = "Attendance UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(attendanceService.getAttendanceById(id));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get member's attendance", description = "Retrieves attendance history for a member")
    public ResponseEntity<Page<AttendanceResponse>> getMemberAttendance(
            @Parameter(description = "Member UUID") @PathVariable UUID memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(attendanceService.getMemberAttendancePaged(memberId, pageable));
    }

    @GetMapping("/report")
    @Operation(summary = "Get attendance report", description = "Generates an attendance report for a date range")
    public ResponseEntity<AttendanceReportResponse> getAttendanceReport(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getAttendanceReport(startDate, endDate));
    }

    @GetMapping("/member/{memberId}/count")
    @Operation(summary = "Get member's visit count", description = "Gets the number of visits for a member in a date range")
    public ResponseEntity<Long> getMemberVisitCount(
            @Parameter(description = "Member UUID") @PathVariable UUID memberId,
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(attendanceService.getMemberVisitCount(memberId, startDate, endDate));
    }
}
