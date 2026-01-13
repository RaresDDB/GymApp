package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.BookTrainingSessionRequest;
import com.gymapp.backend.dtos.responses.TrainingSessionResponse;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.entities.Trainer;
import com.gymapp.backend.entities.TrainingSession;
import com.gymapp.backend.enums.SessionStatus;
import com.gymapp.backend.exceptions.CancellationNotAllowedException;
import com.gymapp.backend.exceptions.InvalidOperationException;
import com.gymapp.backend.exceptions.MembershipExpiredException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.TrainingSessionMapper;
import com.gymapp.backend.repositories.TrainingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainingSessionServiceTest {

    @Mock
    private TrainingSessionRepository sessionRepository;

    @Mock
    private TrainingSessionMapper sessionMapper;

    @Mock
    private MemberService memberService;

    @Mock
    private TrainerService trainerService;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private TrainingSessionService trainingSessionService;

    private UUID sessionId;
    private UUID memberId;
    private UUID trainerId;
    private Member member;
    private Trainer trainer;
    private TrainingSession session;
    private TrainingSessionResponse sessionResponse;
    private BookTrainingSessionRequest bookRequest;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        trainerId = UUID.randomUUID();

        member = Member.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        member.setId(memberId);

        trainer = Trainer.builder()
                .firstName("Mike")
                .lastName("Smith")
                .email("mike.smith@gym.com")
                .specialization("Weightlifting")
                .hourlyRate(BigDecimal.valueOf(50.00))
                .build();
        trainer.setId(trainerId);

        session = TrainingSession.builder()
                .member(member)
                .trainer(trainer)
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .durationMinutes(60)
                .status(SessionStatus.SCHEDULED)
                .notes("Focus on upper body")
                .build();
        session.setId(sessionId);
        session.setCreatedAt(LocalDateTime.now());

        sessionResponse = TrainingSessionResponse.builder()
                .id(sessionId)
                .memberId(memberId)
                .memberName("John Doe")
                .trainerId(trainerId)
                .trainerName("Mike Smith")
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .durationMinutes(60)
                .status(SessionStatus.SCHEDULED)
                .notes("Focus on upper body")
                .build();

        bookRequest = BookTrainingSessionRequest.builder()
                .memberId(memberId)
                .trainerId(trainerId)
                .scheduledAt(LocalDateTime.now().plusDays(2))
                .durationMinutes(60)
                .notes("Focus on upper body")
                .build();
    }

    @Test
    @DisplayName("Should book session successfully")
    void bookSession_Success() {
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(trainerService.findTrainerById(trainerId)).thenReturn(trainer);
        when(subscriptionService.hasActiveSubscription(memberId)).thenReturn(true);
        when(sessionRepository.findConflictingSessions(any(), any(), any())).thenReturn(Collections.emptyList());
        when(sessionRepository.save(any(TrainingSession.class))).thenReturn(session);
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponse);

        TrainingSessionResponse result = trainingSessionService.bookSession(bookRequest);

        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(memberId);
        assertThat(result.getTrainerId()).isEqualTo(trainerId);
        assertThat(result.getStatus()).isEqualTo(SessionStatus.SCHEDULED);
        verify(sessionRepository).save(any(TrainingSession.class));
    }

    @Test
    @DisplayName("Should throw exception when member has no active subscription")
    void bookSession_NoActiveSubscription() {
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(trainerService.findTrainerById(trainerId)).thenReturn(trainer);
        when(subscriptionService.hasActiveSubscription(memberId)).thenReturn(false);

        assertThatThrownBy(() -> trainingSessionService.bookSession(bookRequest))
                .isInstanceOf(MembershipExpiredException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when trainer has conflicting session")
    void bookSession_TrainerNotAvailable() {
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(trainerService.findTrainerById(trainerId)).thenReturn(trainer);
        when(subscriptionService.hasActiveSubscription(memberId)).thenReturn(true);
        when(sessionRepository.findConflictingSessions(any(), any(), any())).thenReturn(List.of(session));

        assertThatThrownBy(() -> trainingSessionService.bookSession(bookRequest))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("not available");

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get session by ID successfully")
    void getSessionById_Success() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponse);

        TrainingSessionResponse result = trainingSessionService.getSessionById(sessionId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(sessionId);
    }

    @Test
    @DisplayName("Should throw exception when session not found by ID")
    void getSessionById_NotFound() {
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainingSessionService.getSessionById(sessionId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get member sessions")
    void getMemberSessions_Success() {
        when(sessionRepository.findByMemberId(memberId)).thenReturn(List.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponse);

        List<TrainingSessionResponse> result = trainingSessionService.getMemberSessions(memberId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMemberId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("Should get member sessions with pagination")
    void getMemberSessionsPaged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TrainingSession> sessionPage = new PageImpl<>(List.of(session));

        when(sessionRepository.findByMemberId(memberId, pageable)).thenReturn(sessionPage);
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponse);

        Page<TrainingSessionResponse> result = trainingSessionService.getMemberSessionsPaged(memberId, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should get trainer sessions")
    void getTrainerSessions_Success() {
        when(sessionRepository.findByTrainerId(trainerId)).thenReturn(List.of(session));
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponse);

        List<TrainingSessionResponse> result = trainingSessionService.getTrainerSessions(trainerId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTrainerId()).isEqualTo(trainerId);
    }

    @Test
    @DisplayName("Should get trainer sessions with pagination")
    void getTrainerSessionsPaged_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<TrainingSession> sessionPage = new PageImpl<>(List.of(session));

        when(sessionRepository.findByTrainerId(trainerId, pageable)).thenReturn(sessionPage);
        when(sessionMapper.toResponse(session)).thenReturn(sessionResponse);

        Page<TrainingSessionResponse> result = trainingSessionService.getTrainerSessionsPaged(trainerId, pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should cancel session successfully")
    void cancelSession_Success() {
        session.setScheduledAt(LocalDateTime.now().plusDays(2));
        TrainingSessionResponse cancelledResponse = TrainingSessionResponse.builder()
                .id(sessionId)
                .status(SessionStatus.NO_SHOW)
                .build();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TrainingSession.class))).thenReturn(session);
        when(sessionMapper.toResponse(any(TrainingSession.class))).thenReturn(cancelledResponse);

        TrainingSessionResponse result = trainingSessionService.cancelSession(sessionId);

        assertThat(result.getStatus()).isEqualTo(SessionStatus.NO_SHOW);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-scheduled session")
    void cancelSession_NotScheduled() {
        session.setStatus(SessionStatus.COMPLETED);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> trainingSessionService.cancelSession(sessionId))
                .isInstanceOf(InvalidOperationException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when cancelling session within 24 hours")
    void cancelSession_TooLate() {
        session.setScheduledAt(LocalDateTime.now().plusHours(12));
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> trainingSessionService.cancelSession(sessionId))
                .isInstanceOf(CancellationNotAllowedException.class);

        verify(sessionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should complete session successfully")
    void completeSession_Success() {
        TrainingSessionResponse completedResponse = TrainingSessionResponse.builder()
                .id(sessionId)
                .status(SessionStatus.COMPLETED)
                .build();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(sessionRepository.save(any(TrainingSession.class))).thenReturn(session);
        when(sessionMapper.toResponse(any(TrainingSession.class))).thenReturn(completedResponse);

        TrainingSessionResponse result = trainingSessionService.completeSession(sessionId);

        assertThat(result.getStatus()).isEqualTo(SessionStatus.COMPLETED);
        verify(sessionRepository).save(session);
    }

    @Test
    @DisplayName("Should throw exception when completing non-scheduled session")
    void completeSession_NotScheduled() {
        session.setStatus(SessionStatus.NO_SHOW);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> trainingSessionService.completeSession(sessionId))
                .isInstanceOf(InvalidOperationException.class);

        verify(sessionRepository, never()).save(any());
    }
}
