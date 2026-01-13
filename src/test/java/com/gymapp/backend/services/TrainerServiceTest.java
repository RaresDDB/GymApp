package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateTrainerRequest;
import com.gymapp.backend.dtos.requests.UpdateTrainerRequest;
import com.gymapp.backend.dtos.responses.TrainerResponse;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.TrainerMapper;
import com.gymapp.backend.entities.Trainer;
import com.gymapp.backend.repositories.TrainerRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrainerServiceTest {

    @Mock
    private TrainerRepository trainerRepository;

    @Mock
    private TrainerMapper trainerMapper;

    @InjectMocks
    private TrainerService trainerService;

    private Trainer trainer;
    private TrainerResponse trainerResponse;
    private CreateTrainerRequest createRequest;
    private UUID trainerId;

    @BeforeEach
    void setUp() {
        trainerId = UUID.randomUUID();

        trainer = Trainer.builder()
                .firstName("Mike")
                .lastName("Smith")
                .email("mike.smith@gym.com")
                .phone("+1234567890")
                .specialization("Strength Training")
                .bio("Certified personal trainer")
                .hourlyRate(BigDecimal.valueOf(50.00))
                .active(true)
                .build();
        trainer.setId(trainerId);
        trainer.setCreatedAt(LocalDateTime.now());
        trainer.setUpdatedAt(LocalDateTime.now());

        trainerResponse = TrainerResponse.builder()
                .id(trainerId)
                .firstName("Mike")
                .lastName("Smith")
                .email("mike.smith@gym.com")
                .phone("+1234567890")
                .specialization("Strength Training")
                .bio("Certified personal trainer")
                .hourlyRate(BigDecimal.valueOf(50.00))
                .active(true)
                .build();

        createRequest = CreateTrainerRequest.builder()
                .firstName("Mike")
                .lastName("Smith")
                .email("mike.smith@gym.com")
                .phone("+1234567890")
                .specialization("Strength Training")
                .bio("Certified personal trainer")
                .hourlyRate(BigDecimal.valueOf(50.00))
                .build();
    }

    @Test
    @DisplayName("Should create trainer successfully")
    void createTrainer_Success() {
        when(trainerRepository.existsByEmail(createRequest.getEmail())).thenReturn(false);
        when(trainerMapper.toEntity(createRequest)).thenReturn(trainer);
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        TrainerResponse result = trainerService.createTrainer(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(createRequest.getEmail());
        verify(trainerRepository).save(any(Trainer.class));
    }

    @Test
    @DisplayName("Should throw exception when creating trainer with existing email")
    void createTrainer_DuplicateEmail() {
        when(trainerRepository.existsByEmail(createRequest.getEmail())).thenReturn(true);

        assertThatThrownBy(() -> trainerService.createTrainer(createRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(trainerRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get trainer by ID successfully")
    void getTrainerById_Success() {
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        TrainerResponse result = trainerService.getTrainerById(trainerId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(trainerId);
    }

    @Test
    @DisplayName("Should throw exception when trainer not found")
    void getTrainerById_NotFound() {
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> trainerService.getTrainerById(trainerId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all active trainers")
    void getAllActiveTrainers_Success() {
        when(trainerRepository.findByActiveTrue()).thenReturn(List.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        List<TrainerResponse> result = trainerService.getAllActiveTrainers();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should get trainers with pagination")
    void getAllTrainers_Paginated() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Trainer> trainerPage = new PageImpl<>(List.of(trainer));

        when(trainerRepository.findByActiveTrue(pageable)).thenReturn(trainerPage);
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        Page<TrainerResponse> result = trainerService.getAllTrainers(pageable);

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("Should get trainers by specialization")
    void getTrainersBySpecialization_Success() {
        when(trainerRepository.findBySpecializationIgnoreCaseAndActiveTrue("Strength Training"))
                .thenReturn(List.of(trainer));
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        List<TrainerResponse> result = trainerService.getTrainersBySpecialization("Strength Training");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getSpecialization()).isEqualTo("Strength Training");
    }

    @Test
    @DisplayName("Should update trainer successfully")
    void updateTrainer_Success() {
        UpdateTrainerRequest updateRequest = UpdateTrainerRequest.builder()
                .hourlyRate(BigDecimal.valueOf(60.00))
                .build();

        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);
        when(trainerMapper.toResponse(trainer)).thenReturn(trainerResponse);

        TrainerResponse result = trainerService.updateTrainer(trainerId, updateRequest);

        assertThat(result).isNotNull();
        verify(trainerMapper).updateEntity(updateRequest, trainer);
        verify(trainerRepository).save(trainer);
    }

    @Test
    @DisplayName("Should soft delete trainer successfully")
    void deleteTrainer_Success() {
        when(trainerRepository.findById(trainerId)).thenReturn(Optional.of(trainer));
        when(trainerRepository.save(any(Trainer.class))).thenReturn(trainer);

        trainerService.deleteTrainer(trainerId);

        assertThat(trainer.isActive()).isFalse();
        verify(trainerRepository).save(trainer);
    }
}
