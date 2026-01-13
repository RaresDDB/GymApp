package com.gymapp.backend.repositories;

import com.gymapp.backend.entities.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByMemberId(UUID memberId);

    Page<Attendance> findByMemberId(UUID memberId, Pageable pageable);

    List<Attendance> findByMemberIdAndVisitDate(UUID memberId, LocalDate visitDate);

    @Query("SELECT a FROM Attendance a WHERE a.member.id = :memberId AND a.checkOutTime IS NULL")
    Optional<Attendance> findActiveCheckIn(@Param("memberId") UUID memberId);

    @Query("SELECT a FROM Attendance a WHERE a.visitDate BETWEEN :startDate AND :endDate")
    List<Attendance> findAttendanceBetweenDates(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.member.id = :memberId AND a.visitDate BETWEEN :startDate AND :endDate")
    long countVisitsByMemberBetweenDates(@Param("memberId") UUID memberId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT a.visitDate, COUNT(a) FROM Attendance a WHERE a.visitDate BETWEEN :startDate AND :endDate GROUP BY a.visitDate ORDER BY a.visitDate")
    List<Object[]> getDailyAttendanceReport(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
