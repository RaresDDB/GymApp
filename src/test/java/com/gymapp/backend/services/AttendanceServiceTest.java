package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CheckInRequest;
import com.gymapp.backend.dtos.responses.AttendanceReportResponse;
import com.gymapp.backend.dtos.responses.AttendanceResponse;
import com.gymapp.backend.exceptions.InvalidOperationException;
import com.gymapp.backend.exceptions.MembershipExpiredException;
import com.gymapp.backend.mappers.AttendanceMapper;
import com.gymapp.backend.entities.Attendance;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.repositories.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock
    private AttendanceRepository attendanceRepository;

    @Mock
    private AttendanceMapper attendanceMapper;

    @Mock
    private MemberService memberService;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private AttendanceService attendanceService;

    private Attendance attendance;
    private AttendanceResponse attendanceResponse;
    private CheckInRequest checkInRequest;
    private Member member;
    private UUID attendanceId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        attendanceId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        member = Member.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .active(true)
                .build();
        member.setId(memberId);

        attendance = Attendance.builder()
                .member(member)
                .checkInTime(LocalDateTime.now())
                .visitDate(LocalDate.now())
                .build();
        attendance.setId(attendanceId);

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
    @DisplayName("Should check in member successfully")
    void checkIn_Success() {
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(subscriptionService.hasActiveSubscription(memberId)).thenReturn(true);
        when(attendanceRepository.findActiveCheckIn(memberId)).thenReturn(Optional.empty());
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        AttendanceResponse result = attendanceService.checkIn(checkInRequest);

        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(memberId);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    @DisplayName("Should throw exception when membership expired")
    void checkIn_MembershipExpired() {
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(subscriptionService.hasActiveSubscription(memberId)).thenReturn(false);

        assertThatThrownBy(() -> attendanceService.checkIn(checkInRequest))
                .isInstanceOf(MembershipExpiredException.class);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when already checked in")
    void checkIn_AlreadyCheckedIn() {
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(subscriptionService.hasActiveSubscription(memberId)).thenReturn(true);
        when(attendanceRepository.findActiveCheckIn(memberId)).thenReturn(Optional.of(attendance));

        assertThatThrownBy(() -> attendanceService.checkIn(checkInRequest))
                .isInstanceOf(InvalidOperationException.class);

        verify(attendanceRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should check out member successfully")
    void checkOut_Success() {
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(attendance);
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        AttendanceResponse result = attendanceService.checkOut(attendanceId);

        assertThat(result).isNotNull();
        verify(attendanceRepository).save(attendance);
    }

    @Test
    @DisplayName("Should throw exception when already checked out")
    void checkOut_AlreadyCheckedOut() {
        attendance.setCheckOutTime(LocalDateTime.now());
        when(attendanceRepository.findById(attendanceId)).thenReturn(Optional.of(attendance));

        assertThatThrownBy(() -> attendanceService.checkOut(attendanceId))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    @DisplayName("Should get member attendance")
    void getMemberAttendance_Success() {
        when(attendanceRepository.findByMemberId(memberId)).thenReturn(List.of(attendance));
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        List<AttendanceResponse> result = attendanceService.getMemberAttendance(memberId);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should generate attendance report")
    void getAttendanceReport_Success() {
        LocalDate startDate = LocalDate.now().minusDays(7);
        LocalDate endDate = LocalDate.now();

        List<Object[]> dailyStats = new ArrayList<>();
        dailyStats.add(new Object[]{LocalDate.now(), 5L});

        when(attendanceRepository.findAttendanceBetweenDates(startDate, endDate))
                .thenReturn(List.of(attendance));
        when(attendanceRepository.getDailyAttendanceReport(startDate, endDate))
                .thenReturn(dailyStats);
        when(attendanceMapper.toResponse(attendance)).thenReturn(attendanceResponse);

        AttendanceReportResponse result = attendanceService.getAttendanceReport(startDate, endDate);

        assertThat(result).isNotNull();
        assertThat(result.getTotalVisits()).isEqualTo(1);
        assertThat(result.getDailyVisits()).containsKey(LocalDate.now());
    }

    @Test
    @DisplayName("Should get member visit count")
    void getMemberVisitCount_Success() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        when(attendanceRepository.countVisitsByMemberBetweenDates(memberId, startDate, endDate))
                .thenReturn(10L);

        long result = attendanceService.getMemberVisitCount(memberId, startDate, endDate);

        assertThat(result).isEqualTo(10L);
    }
}
