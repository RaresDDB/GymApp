package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateTrainerRequest;
import com.gymapp.backend.dtos.requests.UpdateTrainerRequest;
import com.gymapp.backend.dtos.responses.TrainerResponse;
import com.gymapp.backend.dtos.responses.TrainingSessionResponse;
import com.gymapp.backend.services.TrainerService;
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

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainers", description = "Trainer management endpoints")
public class TrainerController {
    private final TrainerService trainerService;
    private final TrainingSessionService sessionService;

    @PostMapping
    @Operation(summary = "Create a new trainer", description = "Registers a new trainer")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trainer created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<TrainerResponse> createTrainer(
            @Valid @RequestBody CreateTrainerRequest request) {
        TrainerResponse response = trainerService.createTrainer(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get trainer by ID", description = "Retrieves a trainer by their UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer found"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    public ResponseEntity<TrainerResponse> getTrainerById(
            @Parameter(description = "Trainer UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(trainerService.getTrainerById(id));
    }

    @GetMapping
    @Operation(summary = "Get all trainers", description = "Retrieves all active trainers with pagination")
    public ResponseEntity<Page<TrainerResponse>> getAllTrainers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(trainerService.getAllTrainers(pageable));
    }

    @GetMapping("/list")
    @Operation(summary = "Get all active trainers", description = "Retrieves all active trainers as a list")
    public ResponseEntity<List<TrainerResponse>> getAllActiveTrainers() {
        return ResponseEntity.ok(trainerService.getAllActiveTrainers());
    }

    @GetMapping("/search")
    @Operation(summary = "Search trainers", description = "Search trainers by name or specialization")
    public ResponseEntity<Page<TrainerResponse>> searchTrainers(
            @Parameter(description = "Search query") @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(trainerService.searchTrainers(query, pageable));
    }

    @GetMapping("/specialization/{specialization}")
    @Operation(summary = "Get trainers by specialization", description = "Retrieves trainers with a specific specialization")
    public ResponseEntity<List<TrainerResponse>> getTrainersBySpecialization(
            @Parameter(description = "Specialization") @PathVariable String specialization) {
        return ResponseEntity.ok(trainerService.getTrainersBySpecialization(specialization));
    }

    @GetMapping("/{id}/sessions")
    @Operation(summary = "Get trainer's sessions", description = "Retrieves all training sessions for a trainer")
    public ResponseEntity<Page<TrainingSessionResponse>> getTrainerSessions(
            @Parameter(description = "Trainer UUID") @PathVariable UUID id,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(sessionService.getTrainerSessionsPaged(id, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update trainer", description = "Updates an existing trainer's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Trainer updated successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<TrainerResponse> updateTrainer(
            @Parameter(description = "Trainer UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateTrainerRequest request) {
        return ResponseEntity.ok(trainerService.updateTrainer(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete trainer", description = "Soft deletes a trainer (deactivates)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trainer deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Trainer not found")
    })
    public ResponseEntity<Void> deleteTrainer(
            @Parameter(description = "Trainer UUID") @PathVariable UUID id) {
        trainerService.deleteTrainer(id);
        return ResponseEntity.noContent().build();
    }
}
