package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.BookTrainingSessionRequest;
import com.gymapp.backend.dtos.responses.TrainingSessionResponse;
import com.gymapp.backend.services.TrainingSessionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/training-sessions")
@RequiredArgsConstructor
@Tag(name = "Training Sessions", description = "Training session management endpoints")
public class TrainingSessionController {
    private final TrainingSessionService sessionService;

    @PostMapping
    @Operation(summary = "Book a training session", description = "Books a new personal training session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session booked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request or trainer not available"),
            @ApiResponse(responseCode = "403", description = "Member's membership has expired"),
            @ApiResponse(responseCode = "404", description = "Member or Trainer not found")
    })
    public ResponseEntity<TrainingSessionResponse> bookSession(
            @Valid @RequestBody BookTrainingSessionRequest request) {
        TrainingSessionResponse response = sessionService.bookSession(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get session by ID", description = "Retrieves a training session by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session found"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<TrainingSessionResponse> getSessionById(
            @Parameter(description = "Session UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.getSessionById(id));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get member's sessions", description = "Retrieves all training sessions for a member")
    public ResponseEntity<Page<TrainingSessionResponse>> getMemberSessions(
            @Parameter(description = "Member UUID") @PathVariable UUID memberId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(sessionService.getMemberSessionsPaged(memberId, pageable));
    }

    @GetMapping("/trainer/{trainerId}")
    @Operation(summary = "Get trainer's sessions", description = "Retrieves all training sessions for a trainer")
    public ResponseEntity<Page<TrainingSessionResponse>> getTrainerSessions(
            @Parameter(description = "Trainer UUID") @PathVariable UUID trainerId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(sessionService.getTrainerSessionsPaged(trainerId, pageable));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel session", description = "Cancels a scheduled training session")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session cancelled"),
            @ApiResponse(responseCode = "400", description = "Session cannot be cancelled (less than 24h before)"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<TrainingSessionResponse> cancelSession(
            @Parameter(description = "Session UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.cancelSession(id));
    }

    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete session", description = "Marks a training session as completed")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session marked as completed"),
            @ApiResponse(responseCode = "400", description = "Session cannot be completed"),
            @ApiResponse(responseCode = "404", description = "Session not found")
    })
    public ResponseEntity<TrainingSessionResponse> completeSession(
            @Parameter(description = "Session UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(sessionService.completeSession(id));
    }
}
