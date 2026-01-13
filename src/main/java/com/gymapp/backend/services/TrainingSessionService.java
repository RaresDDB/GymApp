package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.BookTrainingSessionRequest;
import com.gymapp.backend.dtos.responses.TrainingSessionResponse;
import com.gymapp.backend.exceptions.CancellationNotAllowedException;
import com.gymapp.backend.exceptions.InvalidOperationException;
import com.gymapp.backend.exceptions.MembershipExpiredException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.TrainingSessionMapper;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.entities.Trainer;
import com.gymapp.backend.entities.TrainingSession;
import com.gymapp.backend.enums.SessionStatus;
import com.gymapp.backend.repositories.TrainingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainingSessionService {
    private final TrainingSessionRepository sessionRepository;
    private final TrainingSessionMapper sessionMapper;
    private final MemberService memberService;
    private final TrainerService trainerService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public TrainingSessionResponse bookSession(BookTrainingSessionRequest request) {
        log.info("Booking training session for member: {} with trainer: {}",
                request.getMemberId(), request.getTrainerId());

        Member member = memberService.findMemberById(request.getMemberId());
        Trainer trainer = trainerService.findTrainerById(request.getTrainerId());

        if (!subscriptionService.hasActiveSubscription(request.getMemberId())) {
            throw new MembershipExpiredException(member.getFullName());
        }

        LocalDateTime endTime = request.getScheduledAt().plusMinutes(request.getDurationMinutes());
        List<TrainingSession> conflicts = sessionRepository.findConflictingSessions(
                request.getTrainerId(), request.getScheduledAt(), endTime);

        if (!conflicts.isEmpty()) {
            throw new InvalidOperationException("Trainer is not available at the requested time");
        }

        TrainingSession session = TrainingSession.builder()
                .member(member)
                .trainer(trainer)
                .scheduledAt(request.getScheduledAt())
                .durationMinutes(request.getDurationMinutes())
                .status(SessionStatus.SCHEDULED)
                .notes(request.getNotes())
                .build();

        TrainingSession savedSession = sessionRepository.save(session);

        log.info("Training session booked with ID: {}", savedSession.getId());
        return sessionMapper.toResponse(savedSession);
    }

    @Transactional(readOnly = true)
    public TrainingSessionResponse getSessionById(UUID id) {
        log.debug("Fetching training session with ID: {}", id);
        TrainingSession session = findSessionById(id);
        return sessionMapper.toResponse(session);
    }

    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> getMemberSessions(UUID memberId) {
        log.debug("Fetching sessions for member: {}", memberId);
        return sessionRepository.findByMemberId(memberId).stream()
                .map(sessionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TrainingSessionResponse> getMemberSessionsPaged(UUID memberId, Pageable pageable) {
        log.debug("Fetching sessions for member with pagination: {}", memberId);
        return sessionRepository.findByMemberId(memberId, pageable)
                .map(sessionMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TrainingSessionResponse> getTrainerSessions(UUID trainerId) {
        log.debug("Fetching sessions for trainer: {}", trainerId);
        return sessionRepository.findByTrainerId(trainerId).stream()
                .map(sessionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TrainingSessionResponse> getTrainerSessionsPaged(UUID trainerId, Pageable pageable) {
        log.debug("Fetching sessions for trainer with pagination: {}", trainerId);
        return sessionRepository.findByTrainerId(trainerId, pageable)
                .map(sessionMapper::toResponse);
    }

    @Transactional
    public TrainingSessionResponse cancelSession(UUID id) {
        log.info("Cancelling training session with ID: {}", id);

        TrainingSession session = findSessionById(id);

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new InvalidOperationException("Only scheduled sessions can be cancelled");
        }

        if (session.getScheduledAt().minusHours(24).isBefore(LocalDateTime.now())) {
            throw new CancellationNotAllowedException();
        }

        session.setStatus(SessionStatus.NO_SHOW);
        TrainingSession updatedSession = sessionRepository.save(session);

        log.info("Training session cancelled with ID: {}", id);
        return sessionMapper.toResponse(updatedSession);
    }

    @Transactional
    public TrainingSessionResponse completeSession(UUID id) {
        log.info("Completing training session with ID: {}", id);

        TrainingSession session = findSessionById(id);

        if (session.getStatus() != SessionStatus.SCHEDULED) {
            throw new InvalidOperationException("Only scheduled sessions can be completed");
        }

        session.setStatus(SessionStatus.COMPLETED);
        TrainingSession updatedSession = sessionRepository.save(session);

        log.info("Training session completed with ID: {}", id);
        return sessionMapper.toResponse(updatedSession);
    }

    @Transactional(readOnly = true)
    public TrainingSession findSessionById(UUID id) {
        return sessionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TrainingSession", "id", id));
    }
}
