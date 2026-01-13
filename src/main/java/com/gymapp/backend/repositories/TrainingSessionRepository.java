package com.gymapp.backend.repositories;

import com.gymapp.backend.entities.TrainingSession;
import com.gymapp.backend.enums.SessionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, UUID> {
    List<TrainingSession> findByMemberId(UUID memberId);

    Page<TrainingSession> findByMemberId(UUID memberId, Pageable pageable);

    List<TrainingSession> findByTrainerId(UUID trainerId);

    Page<TrainingSession> findByTrainerId(UUID trainerId, Pageable pageable);

    List<TrainingSession> findByTrainerIdAndScheduledAtBetween(UUID trainerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT ts FROM TrainingSession ts WHERE ts.trainer.id = :trainerId " +
            "AND ts.scheduledAt >= :start AND ts.scheduledAt <= :end " +
            "AND ts.status = 'SCHEDULED'")
    List<TrainingSession> findConflictingSessions(
            @Param("trainerId") UUID trainerId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    List<TrainingSession> findByStatus(SessionStatus status);

    @Query("SELECT ts FROM TrainingSession ts WHERE ts.scheduledAt < :dateTime AND ts.status = 'SCHEDULED'")
    List<TrainingSession> findPastScheduledSessions(@Param("dateTime") LocalDateTime dateTime);
}
