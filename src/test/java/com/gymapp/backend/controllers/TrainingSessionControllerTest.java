package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.BookTrainingSessionRequest;
import com.gymapp.backend.dtos.responses.TrainingSessionResponse;
import com.gymapp.backend.enums.SessionStatus;
import com.gymapp.backend.exceptions.CancellationNotAllowedException;
import com.gymapp.backend.exceptions.GlobalExceptionHandler;
import com.gymapp.backend.exceptions.InvalidOperationException;
import com.gymapp.backend.exceptions.MembershipExpiredException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.services.TrainingSessionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingSessionController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureJsonTesters
class TrainingSessionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<BookTrainingSessionRequest> bookTrainingSessionRequestJson;

    @MockitoBean
    @SuppressWarnings("unused")
    private TrainingSessionService sessionService;

    private UUID sessionId;
    private UUID memberId;
    private UUID trainerId;
    private TrainingSessionResponse sessionResponse;
    private BookTrainingSessionRequest bookRequest;

    @BeforeEach
    void setUp() {
        sessionId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        trainerId = UUID.randomUUID();

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
                .createdAt(LocalDateTime.now())
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
    @DisplayName("POST /api/training-sessions - Should book session successfully")
    void bookSession_Success() throws Exception {
        when(sessionService.bookSession(any(BookTrainingSessionRequest.class))).thenReturn(sessionResponse);

        mockMvc.perform(
                post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookTrainingSessionRequestJson.write(bookRequest).getJson())
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST /api/training-sessions - Should return 400 for invalid request")
    void bookSession_InvalidRequest() throws Exception {
        BookTrainingSessionRequest invalidRequest = BookTrainingSessionRequest.builder()
                .memberId(null)
                .trainerId(null)
                .scheduledAt(null)
                .build();

        mockMvc.perform(
                post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookTrainingSessionRequestJson.write(invalidRequest).getJson())
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/training-sessions - Should return 403 for expired membership")
    void bookSession_MembershipExpired() throws Exception {
        when(sessionService.bookSession(any(BookTrainingSessionRequest.class)))
                .thenThrow(new MembershipExpiredException("John Doe"));

        mockMvc.perform(
                post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookTrainingSessionRequestJson.write(bookRequest).getJson())
        )
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/training-sessions - Should return 400 when trainer not available")
    void bookSession_TrainerNotAvailable() throws Exception {
        when(sessionService.bookSession(any(BookTrainingSessionRequest.class)))
                .thenThrow(new InvalidOperationException("Trainer is not available at the requested time"));

        mockMvc.perform(
                post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookTrainingSessionRequestJson.write(bookRequest).getJson())
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/training-sessions/{id} - Should return session by ID")
    void getSessionById_Success() throws Exception {
        when(sessionService.getSessionById(sessionId)).thenReturn(sessionResponse);

        mockMvc.perform(get("/api/training-sessions/{id}", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(sessionId.toString()))
                .andExpect(jsonPath("$.memberName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/training-sessions/{id} - Should return 404 for non-existent session")
    void getSessionById_NotFound() throws Exception {
        when(sessionService.getSessionById(sessionId))
                .thenThrow(new ResourceNotFoundException("TrainingSession", "id", sessionId));

        mockMvc.perform(get("/api/training-sessions/{id}", sessionId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/training-sessions/member/{memberId} - Should return member's sessions")
    void getMemberSessions_Success() throws Exception {
        Page<TrainingSessionResponse> page = new PageImpl<>(List.of(sessionResponse));
        when(sessionService.getMemberSessionsPaged(eq(memberId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/training-sessions/member/{memberId}", memberId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].memberId").value(memberId.toString()));
    }

    @Test
    @DisplayName("GET /api/training-sessions/trainer/{trainerId} - Should return trainer's sessions")
    void getTrainerSessions_Success() throws Exception {
        Page<TrainingSessionResponse> page = new PageImpl<>(List.of(sessionResponse));
        when(sessionService.getTrainerSessionsPaged(eq(trainerId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/training-sessions/trainer/{trainerId}", trainerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].trainerId").value(trainerId.toString()));
    }

    @Test
    @DisplayName("PUT /api/training-sessions/{id}/cancel - Should cancel session")
    void cancelSession_Success() throws Exception {
        TrainingSessionResponse cancelledResponse = TrainingSessionResponse.builder()
                .id(sessionId)
                .status(SessionStatus.NO_SHOW)
                .build();

        when(sessionService.cancelSession(sessionId)).thenReturn(cancelledResponse);

        mockMvc.perform(put("/api/training-sessions/{id}/cancel", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("NO_SHOW"));
    }

    @Test
    @DisplayName("PUT /api/training-sessions/{id}/cancel - Should return 400 for non-scheduled session")
    void cancelSession_NotScheduled() throws Exception {
        when(sessionService.cancelSession(sessionId))
                .thenThrow(new InvalidOperationException("Only scheduled sessions can be cancelled"));

        mockMvc.perform(put("/api/training-sessions/{id}/cancel", sessionId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/training-sessions/{id}/cancel - Should return 400 when too late to cancel")
    void cancelSession_TooLate() throws Exception {
        when(sessionService.cancelSession(sessionId))
                .thenThrow(new CancellationNotAllowedException());

        mockMvc.perform(put("/api/training-sessions/{id}/cancel", sessionId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/training-sessions/{id}/complete - Should complete session")
    void completeSession_Success() throws Exception {
        TrainingSessionResponse completedResponse = TrainingSessionResponse.builder()
                .id(sessionId)
                .status(SessionStatus.COMPLETED)
                .build();

        when(sessionService.completeSession(sessionId)).thenReturn(completedResponse);

        mockMvc.perform(put("/api/training-sessions/{id}/complete", sessionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    @DisplayName("PUT /api/training-sessions/{id}/complete - Should return 400 for non-scheduled session")
    void completeSession_NotScheduled() throws Exception {
        when(sessionService.completeSession(sessionId))
                .thenThrow(new InvalidOperationException("Only scheduled sessions can be completed"));

        mockMvc.perform(put("/api/training-sessions/{id}/complete", sessionId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/training-sessions/{id}/complete - Should return 404 for non-existent session")
    void completeSession_NotFound() throws Exception {
        when(sessionService.completeSession(sessionId))
                .thenThrow(new ResourceNotFoundException("TrainingSession", "id", sessionId));

        mockMvc.perform(put("/api/training-sessions/{id}/complete", sessionId))
                .andExpect(status().isNotFound());
    }
}
