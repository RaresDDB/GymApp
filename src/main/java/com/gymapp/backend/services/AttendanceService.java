package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CheckInRequest;
import com.gymapp.backend.dtos.responses.AttendanceReportResponse;
import com.gymapp.backend.dtos.responses.AttendanceResponse;
import com.gymapp.backend.exceptions.InvalidOperationException;
import com.gymapp.backend.exceptions.MembershipExpiredException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.AttendanceMapper;
import com.gymapp.backend.entities.Attendance;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.repositories.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final AttendanceMapper attendanceMapper;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public AttendanceResponse checkIn(CheckInRequest request) {
        log.info("Member check-in: {}", request.getMemberId());

        Member member = memberService.findMemberById(request.getMemberId());

        if (!subscriptionService.hasActiveSubscription(request.getMemberId())) {
            throw new MembershipExpiredException(member.getFullName());
        }

        if (attendanceRepository.findActiveCheckIn(request.getMemberId()).isPresent()) {
            throw new InvalidOperationException("Member already checked in. Please check out first.");
        }

        Attendance attendance = Attendance.builder()
                .member(member)
                .checkInTime(LocalDateTime.now())
                .visitDate(LocalDate.now())
                .build();

        Attendance savedAttendance = attendanceRepository.save(attendance);

        log.info("Member checked in successfully with ID: {}", savedAttendance.getId());
        return attendanceMapper.toResponse(savedAttendance);
    }

    @Transactional
    public AttendanceResponse checkOut(UUID attendanceId) {
        log.info("Member check-out for attendance: {}", attendanceId);

        Attendance attendance = findAttendanceById(attendanceId);

        if (attendance.getCheckOutTime() != null) {
            throw new InvalidOperationException("Member has already checked out");
        }

        attendance.setCheckOutTime(LocalDateTime.now());
        Attendance updatedAttendance = attendanceRepository.save(attendance);

        log.info("Member checked out successfully");
        return attendanceMapper.toResponse(updatedAttendance);
    }

    @Transactional(readOnly = true)
    public AttendanceResponse getAttendanceById(UUID id) {
        log.debug("Fetching attendance with ID: {}", id);
        Attendance attendance = findAttendanceById(id);
        return attendanceMapper.toResponse(attendance);
    }

    @Transactional(readOnly = true)
    public List<AttendanceResponse> getMemberAttendance(UUID memberId) {
        log.debug("Fetching attendance for member: {}", memberId);
        return attendanceRepository.findByMemberId(memberId).stream()
                .map(attendanceMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<AttendanceResponse> getMemberAttendancePaged(UUID memberId, Pageable pageable) {
        log.debug("Fetching attendance for member with pagination: {}", memberId);
        return attendanceRepository.findByMemberId(memberId, pageable)
                .map(attendanceMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public AttendanceReportResponse getAttendanceReport(LocalDate startDate, LocalDate endDate) {
        log.debug("Generating attendance report from {} to {}", startDate, endDate);

        List<Attendance> attendanceList = attendanceRepository.findAttendanceBetweenDates(startDate, endDate);
        List<Object[]> dailyStats = attendanceRepository.getDailyAttendanceReport(startDate, endDate);

        Map<LocalDate, Long> dailyVisits = new LinkedHashMap<>();
        for (Object[] stat : dailyStats) {
            dailyVisits.put((LocalDate) stat[0], (Long) stat[1]);
        }

        List<AttendanceResponse> recentAttendance = attendanceList.stream()
                .limit(100)
                .map(attendanceMapper::toResponse)
                .collect(Collectors.toList());

        return AttendanceReportResponse.builder()
                .startDate(startDate)
                .endDate(endDate)
                .totalVisits(attendanceList.size())
                .dailyVisits(dailyVisits)
                .recentAttendance(recentAttendance)
                .build();
    }

    @Transactional(readOnly = true)
    public long getMemberVisitCount(UUID memberId, LocalDate startDate, LocalDate endDate) {
        log.debug("Getting visit count for member {} between {} and {}", memberId, startDate, endDate);
        return attendanceRepository.countVisitsByMemberBetweenDates(memberId, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Attendance findAttendanceById(UUID id) {
        return attendanceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", "id", id));
    }
}
