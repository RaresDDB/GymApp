package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateMembershipPlanRequest;
import com.gymapp.backend.dtos.requests.UpdateMembershipPlanRequest;
import com.gymapp.backend.dtos.responses.MembershipPlanResponse;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.MembershipPlanMapper;
import com.gymapp.backend.entities.MembershipPlan;
import com.gymapp.backend.repositories.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MembershipPlanService {
    private final MembershipPlanRepository planRepository;
    private final MembershipPlanMapper planMapper;

    @Transactional
    public MembershipPlanResponse createPlan(CreateMembershipPlanRequest request) {
        log.info("Creating new membership plan: {}", request.getName());

        if (planRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("MembershipPlan", "name", request.getName());
        }

        MembershipPlan plan = planMapper.toEntity(request);
        MembershipPlan savedPlan = planRepository.save(plan);

        log.info("Membership plan created with ID: {}", savedPlan.getId());
        return planMapper.toResponse(savedPlan);
    }

    @Transactional(readOnly = true)
    public MembershipPlanResponse getPlanById(UUID id) {
        log.debug("Fetching membership plan with ID: {}", id);
        MembershipPlan plan = findPlanById(id);
        return planMapper.toResponse(plan);
    }

    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> getAllActivePlans() {
        log.debug("Fetching all active membership plans");
        return planRepository.findByActiveTrueOrderByPriceAsc().stream()
                .map(planMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> getAllPlans() {
        log.debug("Fetching all membership plans");
        return planRepository.findAll().stream()
                .map(planMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public MembershipPlanResponse updatePlan(UUID id, UpdateMembershipPlanRequest request) {
        log.info("Updating membership plan with ID: {}", id);

        MembershipPlan plan = findPlanById(id);

        if (request.getName() != null && !request.getName().equals(plan.getName())) {
            if (planRepository.existsByName(request.getName())) {
                throw new DuplicateResourceException("MembershipPlan", "name", request.getName());
            }
        }

        planMapper.updateEntity(request, plan);
        MembershipPlan updatedPlan = planRepository.save(plan);

        log.info("Membership plan updated with ID: {}", updatedPlan.getId());
        return planMapper.toResponse(updatedPlan);
    }

    @Transactional
    public void deletePlan(UUID id) {
        log.info("Deleting membership plan with ID: {}", id);
        MembershipPlan plan = findPlanById(id);
        plan.setActive(false);
        planRepository.save(plan);
        log.info("Membership plan deactivated with ID: {}", id);
    }

    @Transactional(readOnly = true)
    public MembershipPlan findPlanById(UUID id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", "id", id));
    }
}
