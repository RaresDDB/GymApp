package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateTrainerRequest;
import com.gymapp.backend.dtos.requests.UpdateTrainerRequest;
import com.gymapp.backend.dtos.responses.TrainerResponse;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.TrainerMapper;
import com.gymapp.backend.entities.Trainer;
import com.gymapp.backend.repositories.TrainerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TrainerService {
    private final TrainerRepository trainerRepository;
    private final TrainerMapper trainerMapper;

    @Transactional
    public TrainerResponse createTrainer(CreateTrainerRequest request) {
        log.info("Creating new trainer with email: {}", request.getEmail());

        if (trainerRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Trainer", "email", request.getEmail());
        }

        Trainer trainer = trainerMapper.toEntity(request);
        Trainer savedTrainer = trainerRepository.save(trainer);

        log.info("Trainer created with ID: {}", savedTrainer.getId());
        return trainerMapper.toResponse(savedTrainer);
    }

    @Transactional(readOnly = true)
    public TrainerResponse getTrainerById(UUID id) {
        log.debug("Fetching trainer with ID: {}", id);
        Trainer trainer = findTrainerById(id);
        return trainerMapper.toResponse(trainer);
    }

    @Transactional(readOnly = true)
    public List<TrainerResponse> getAllActiveTrainers() {
        log.debug("Fetching all active trainers");
        return trainerRepository.findByActiveTrue().stream()
                .map(trainerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<TrainerResponse> getAllTrainers(Pageable pageable) {
        log.debug("Fetching all trainers with pagination");
        return trainerRepository.findByActiveTrue(pageable)
                .map(trainerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TrainerResponse> searchTrainers(String search, Pageable pageable) {
        log.debug("Searching trainers with query: {}", search);
        return trainerRepository.searchTrainers(search, pageable)
                .map(trainerMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<TrainerResponse> getTrainersBySpecialization(String specialization) {
        log.debug("Fetching trainers by specialization: {}", specialization);
        return trainerRepository.findBySpecializationIgnoreCaseAndActiveTrue(specialization).stream()
                .map(trainerMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public TrainerResponse updateTrainer(UUID id, UpdateTrainerRequest request) {
        log.info("Updating trainer with ID: {}", id);

        Trainer trainer = findTrainerById(id);

        if (request.getEmail() != null && !request.getEmail().equals(trainer.getEmail())) {
            if (trainerRepository.existsByEmail(request.getEmail())) {
                throw new DuplicateResourceException("Trainer", "email", request.getEmail());
            }
        }

        trainerMapper.updateEntity(request, trainer);
        Trainer updatedTrainer = trainerRepository.save(trainer);

        log.info("Trainer updated with ID: {}", updatedTrainer.getId());
        return trainerMapper.toResponse(updatedTrainer);
    }

    @Transactional
    public void deleteTrainer(UUID id) {
        log.info("Deleting trainer with ID: {}", id);
        Trainer trainer = findTrainerById(id);
        trainer.setActive(false);
        trainerRepository.save(trainer);
        log.info("Trainer deactivated with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public Trainer findTrainerById(UUID id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "id", id));
    }
}
