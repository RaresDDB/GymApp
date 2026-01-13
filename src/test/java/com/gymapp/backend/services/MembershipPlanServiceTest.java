package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateMembershipPlanRequest;
import com.gymapp.backend.dtos.requests.UpdateMembershipPlanRequest;
import com.gymapp.backend.dtos.responses.MembershipPlanResponse;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.MembershipPlanMapper;
import com.gymapp.backend.entities.MembershipPlan;
import com.gymapp.backend.repositories.MembershipPlanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class MembershipPlanServiceTest {

    @Mock
    private MembershipPlanRepository planRepository;

    @Mock
    private MembershipPlanMapper planMapper;

    @InjectMocks
    private MembershipPlanService planService;

    private MembershipPlan plan;
    private MembershipPlanResponse planResponse;
    private CreateMembershipPlanRequest createRequest;
    private UUID planId;

    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();

        plan = MembershipPlan.builder()
                .name("Premium")
                .description("Premium membership with all features")
                .price(BigDecimal.valueOf(99.99))
                .durationMonths(12)
                .includesPersonalTraining(true)
                .includesGroupClasses(true)
                .maxClassesPerMonth(20)
                .active(true)
                .build();
        plan.setId(planId);
        plan.setCreatedAt(LocalDateTime.now());
        plan.setUpdatedAt(LocalDateTime.now());

        planResponse = MembershipPlanResponse.builder()
                .id(planId)
                .name("Premium")
                .description("Premium membership with all features")
                .price(BigDecimal.valueOf(99.99))
                .durationMonths(12)
                .includesPersonalTraining(true)
                .includesGroupClasses(true)
                .maxClassesPerMonth(20)
                .active(true)
                .build();

        createRequest = CreateMembershipPlanRequest.builder()
                .name("Premium")
                .description("Premium membership with all features")
                .price(BigDecimal.valueOf(99.99))
                .durationMonths(12)
                .includesPersonalTraining(true)
                .includesGroupClasses(true)
                .maxClassesPerMonth(20)
                .build();
    }

    @Test
    @DisplayName("Should create plan successfully")
    void createPlan_Success() {
        when(planRepository.existsByName(createRequest.getName())).thenReturn(false);
        when(planMapper.toEntity(createRequest)).thenReturn(plan);
        when(planRepository.save(any(MembershipPlan.class))).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponse);

        MembershipPlanResponse result = planService.createPlan(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(createRequest.getName());
        verify(planRepository).save(any(MembershipPlan.class));
    }

    @Test
    @DisplayName("Should throw exception when creating plan with existing name")
    void createPlan_DuplicateName() {
        when(planRepository.existsByName(createRequest.getName())).thenReturn(true);

        assertThatThrownBy(() -> planService.createPlan(createRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(planRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get plan by ID successfully")
    void getPlanById_Success() {
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planMapper.toResponse(plan)).thenReturn(planResponse);

        MembershipPlanResponse result = planService.getPlanById(planId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(planId);
    }

    @Test
    @DisplayName("Should throw exception when plan not found")
    void getPlanById_NotFound() {
        when(planRepository.findById(planId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> planService.getPlanById(planId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all active plans")
    void getAllActivePlans_Success() {
        when(planRepository.findByActiveTrueOrderByPriceAsc()).thenReturn(List.of(plan));
        when(planMapper.toResponse(plan)).thenReturn(planResponse);

        List<MembershipPlanResponse> result = planService.getAllActivePlans();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo(plan.getName());
    }

    @Test
    @DisplayName("Should update plan successfully")
    void updatePlan_Success() {
        UpdateMembershipPlanRequest updateRequest = UpdateMembershipPlanRequest.builder()
                .price(BigDecimal.valueOf(129.99))
                .build();

        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(MembershipPlan.class))).thenReturn(plan);
        when(planMapper.toResponse(plan)).thenReturn(planResponse);

        MembershipPlanResponse result = planService.updatePlan(planId, updateRequest);

        assertThat(result).isNotNull();
        verify(planMapper).updateEntity(updateRequest, plan);
        verify(planRepository).save(plan);
    }

    @Test
    @DisplayName("Should soft delete plan successfully")
    void deletePlan_Success() {
        when(planRepository.findById(planId)).thenReturn(Optional.of(plan));
        when(planRepository.save(any(MembershipPlan.class))).thenReturn(plan);

        planService.deletePlan(planId);

        assertThat(plan.isActive()).isFalse();
        verify(planRepository).save(plan);
    }
}
