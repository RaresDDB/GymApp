package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateTrainerRequest;
import com.gymapp.backend.dtos.requests.UpdateTrainerRequest;
import com.gymapp.backend.dtos.responses.TrainerResponse;
import com.gymapp.backend.dtos.responses.TrainingSessionResponse;
import com.gymapp.backend.enums.SessionStatus;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.GlobalExceptionHandler;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.services.TrainerService;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureJsonTesters
class TrainerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<CreateTrainerRequest> createTrainerRequestJson;

    @Autowired
    private JacksonTester<UpdateTrainerRequest> updateTrainerRequestJson;

    @MockitoBean
    @SuppressWarnings("unused")
    private TrainerService trainerService;

    @MockitoBean
    @SuppressWarnings("unused")
    private TrainingSessionService sessionService;

    private UUID trainerId;
    private TrainerResponse trainerResponse;
    private CreateTrainerRequest createRequest;

    @BeforeEach
    void setUp() {
        trainerId = UUID.randomUUID();

        trainerResponse = TrainerResponse.builder()
                .id(trainerId)
                .firstName("Mike")
                .lastName("Smith")
                .email("mike.smith@gym.com")
                .phone("+1234567890")
                .specialization("Weightlifting")
                .bio("Professional trainer with 10 years experience")
                .hourlyRate(BigDecimal.valueOf(50.00))
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        createRequest = CreateTrainerRequest.builder()
                .firstName("Mike")
                .lastName("Smith")
                .email("mike.smith@gym.com")
                .phone("+1234567890")
                .specialization("Weightlifting")
                .bio("Professional trainer with 10 years experience")
                .hourlyRate(BigDecimal.valueOf(50.00))
                .build();
    }

    @Test
    @DisplayName("POST /api/trainers - Should create trainer successfully")
    void createTrainer_Success() throws Exception {
        when(trainerService.createTrainer(any(CreateTrainerRequest.class))).thenReturn(trainerResponse);

        mockMvc.perform(
                post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTrainerRequestJson.write(createRequest).getJson())
        )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(trainerId.toString()))
                .andExpect(jsonPath("$.email").value("mike.smith@gym.com"));
    }

    @Test
    @DisplayName("POST /api/trainers - Should return 400 for invalid request")
    void createTrainer_InvalidRequest() throws Exception {
        CreateTrainerRequest invalidRequest = CreateTrainerRequest.builder()
                .firstName("")
                .email("invalid-email")
                .build();

        mockMvc.perform(
                post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTrainerRequestJson.write(invalidRequest).getJson())
        )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/trainers - Should return 409 for duplicate email")
    void createTrainer_DuplicateEmail() throws Exception {
        when(trainerService.createTrainer(any(CreateTrainerRequest.class)))
                .thenThrow(new DuplicateResourceException("Trainer", "email", "mike.smith@gym.com"));

        mockMvc.perform(
                post("/api/trainers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createTrainerRequestJson.write(createRequest).getJson())
        )
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/trainers/{id} - Should return trainer by ID")
    void getTrainerById_Success() throws Exception {
        when(trainerService.getTrainerById(trainerId)).thenReturn(trainerResponse);

        mockMvc.perform(get("/api/trainers/{id}", trainerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(trainerId.toString()))
                .andExpect(jsonPath("$.firstName").value("Mike"));
    }

    @Test
    @DisplayName("GET /api/trainers/{id} - Should return 404 for non-existent trainer")
    void getTrainerById_NotFound() throws Exception {
        when(trainerService.getTrainerById(trainerId))
                .thenThrow(new ResourceNotFoundException("Trainer", "id", trainerId));

        mockMvc.perform(get("/api/trainers/{id}", trainerId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/trainers - Should return paginated trainers")
    void getAllTrainers_Success() throws Exception {
        Page<TrainerResponse> page = new PageImpl<>(List.of(trainerResponse));
        when(trainerService.getAllTrainers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(trainerId.toString()));
    }

    @Test
    @DisplayName("GET /api/trainers/list - Should return all active trainers")
    void getAllActiveTrainers_Success() throws Exception {
        when(trainerService.getAllActiveTrainers()).thenReturn(List.of(trainerResponse));

        mockMvc.perform(get("/api/trainers/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(trainerId.toString()));
    }

    @Test
    @DisplayName("GET /api/trainers/search - Should search trainers")
    void searchTrainers_Success() throws Exception {
        Page<TrainerResponse> page = new PageImpl<>(List.of(trainerResponse));
        when(trainerService.searchTrainers(eq("Mike"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/trainers/search").param("query", "Mike"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].firstName").value("Mike"));
    }

    @Test
    @DisplayName("GET /api/trainers/specialization/{specialization} - Should return trainers by specialization")
    void getTrainersBySpecialization_Success() throws Exception {
        when(trainerService.getTrainersBySpecialization("Weightlifting")).thenReturn(List.of(trainerResponse));

        mockMvc.perform(get("/api/trainers/specialization/{specialization}", "Weightlifting"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].specialization").value("Weightlifting"));
    }

    @Test
    @DisplayName("GET /api/trainers/{id}/sessions - Should return trainer's sessions")
    void getTrainerSessions_Success() throws Exception {
        TrainingSessionResponse sessionResponse = TrainingSessionResponse.builder()
                .id(UUID.randomUUID())
                .trainerId(trainerId)
                .trainerName("Mike Smith")
                .status(SessionStatus.SCHEDULED)
                .build();
        Page<TrainingSessionResponse> page = new PageImpl<>(List.of(sessionResponse));

        when(sessionService.getTrainerSessionsPaged(eq(trainerId), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/trainers/{id}/sessions", trainerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].trainerId").value(trainerId.toString()));
    }

    @Test
    @DisplayName("PUT /api/trainers/{id} - Should update trainer successfully")
    void updateTrainer_Success() throws Exception {
        UpdateTrainerRequest updateRequest = UpdateTrainerRequest.builder()
                .firstName("Michael")
                .build();

        when(trainerService.updateTrainer(eq(trainerId), any(UpdateTrainerRequest.class)))
                .thenReturn(trainerResponse);

        mockMvc.perform(
                put("/api/trainers/{id}", trainerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateTrainerRequestJson.write(updateRequest).getJson())
        )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/trainers/{id} - Should delete trainer successfully")
    void deleteTrainer_Success() throws Exception {
        doNothing().when(trainerService).deleteTrainer(trainerId);

        mockMvc.perform(delete("/api/trainers/{id}", trainerId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/trainers/{id} - Should return 404 for non-existent trainer")
    void deleteTrainer_NotFound() throws Exception {
        when(trainerService.getTrainerById(trainerId))
                .thenThrow(new ResourceNotFoundException("Trainer", "id", trainerId));

        mockMvc.perform(get("/api/trainers/{id}", trainerId))
                .andExpect(status().isNotFound());
    }
}
