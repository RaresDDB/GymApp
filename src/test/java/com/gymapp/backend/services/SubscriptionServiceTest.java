package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateSubscriptionRequest;
import com.gymapp.backend.dtos.responses.SubscriptionResponse;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.entities.MembershipPlan;
import com.gymapp.backend.entities.Subscription;
import com.gymapp.backend.enums.SubscriptionStatus;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.InvalidOperationException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.SubscriptionMapper;
import com.gymapp.backend.repositories.SubscriptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionMapper subscriptionMapper;

    @Mock
    private MemberService memberService;

    @Mock
    private MembershipPlanService planService;

    @InjectMocks
    private SubscriptionService subscriptionService;

    private UUID subscriptionId;
    private UUID memberId;
    private UUID planId;
    private Member member;
    private MembershipPlan plan;
    private Subscription subscription;
    private SubscriptionResponse subscriptionResponse;
    private CreateSubscriptionRequest createRequest;

    @BeforeEach
    void setUp() {
        subscriptionId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        planId = UUID.randomUUID();

        member = Member.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .build();
        member.setId(memberId);

        plan = MembershipPlan.builder()
                .name("Premium")
                .price(BigDecimal.valueOf(99.99))
                .durationMonths(3)
                .build();
        plan.setId(planId);

        subscription = Subscription.builder()
                .member(member)
                .membershipPlan(plan)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        subscription.setId(subscriptionId);
        subscription.setCreatedAt(LocalDateTime.now());

        subscriptionResponse = SubscriptionResponse.builder()
                .id(subscriptionId)
                .memberId(memberId)
                .memberName("John Doe")
                .planId(planId)
                .planName("Premium")
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(3))
                .status(SubscriptionStatus.ACTIVE)
                .build();

        createRequest = CreateSubscriptionRequest.builder()
                .memberId(memberId)
                .planId(planId)
                .startDate(LocalDate.now())
                .build();
    }

    @Test
    @DisplayName("Should create subscription successfully")
    void createSubscription_Success() {
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(planService.findPlanById(planId)).thenReturn(plan);
        when(subscriptionRepository.existsByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE)).thenReturn(false);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        SubscriptionResponse result = subscriptionService.createSubscription(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(memberId);
        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    @DisplayName("Should throw exception when member already has active subscription")
    void createSubscription_DuplicateActiveSubscription() {
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(planService.findPlanById(planId)).thenReturn(plan);
        when(subscriptionRepository.existsByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE)).thenReturn(true);

        assertThatThrownBy(() -> subscriptionService.createSubscription(createRequest))
                .isInstanceOf(DuplicateResourceException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get subscription by ID successfully")
    void getSubscriptionById_Success() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        SubscriptionResponse result = subscriptionService.getSubscriptionById(subscriptionId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(subscriptionId);
    }

    @Test
    @DisplayName("Should throw exception when subscription not found by ID")
    void getSubscriptionById_NotFound() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getSubscriptionById(subscriptionId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get member subscription successfully")
    void getMemberSubscription_Success() {
        when(subscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.of(subscription));
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        SubscriptionResponse result = subscriptionService.getMemberSubscription(memberId);

        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(memberId);
    }

    @Test
    @DisplayName("Should throw exception when member subscription not found")
    void getMemberSubscription_NotFound() {
        when(subscriptionRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.getMemberSubscription(memberId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should cancel subscription successfully")
    void cancelSubscription_Success() {
        SubscriptionResponse cancelledResponse = SubscriptionResponse.builder()
                .id(subscriptionId)
                .status(SubscriptionStatus.CANCELLED)
                .build();

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(subscriptionMapper.toResponse(any(Subscription.class))).thenReturn(cancelledResponse);

        SubscriptionResponse result = subscriptionService.cancelSubscription(subscriptionId);

        assertThat(result.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    @DisplayName("Should throw exception when cancelling non-active subscription")
    void cancelSubscription_NotActive() {
        subscription.setStatus(SubscriptionStatus.CANCELLED);
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));

        assertThatThrownBy(() -> subscriptionService.cancelSubscription(subscriptionId))
                .isInstanceOf(InvalidOperationException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should renew subscription successfully")
    void renewSubscription_Success() {
        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        SubscriptionResponse result = subscriptionService.renewSubscription(subscriptionId, null);

        assertThat(result).isNotNull();
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    @DisplayName("Should renew subscription with new plan")
    void renewSubscription_WithNewPlan() {
        UUID newPlanId = UUID.randomUUID();
        MembershipPlan newPlan = MembershipPlan.builder()
                .name("VIP")
                .price(BigDecimal.valueOf(199.99))
                .durationMonths(6)
                .build();
        newPlan.setId(newPlanId);

        when(subscriptionRepository.findById(subscriptionId)).thenReturn(Optional.of(subscription));
        when(planService.findPlanById(newPlanId)).thenReturn(newPlan);
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        SubscriptionResponse result = subscriptionService.renewSubscription(subscriptionId, newPlanId);

        assertThat(result).isNotNull();
        verify(planService).findPlanById(newPlanId);
        verify(subscriptionRepository).save(subscription);
    }

    @Test
    @DisplayName("Should get expiring subscriptions")
    void getExpiringSubscriptions_Success() {
        when(subscriptionRepository.findSubscriptionsExpiringBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of(subscription));
        when(subscriptionMapper.toResponse(subscription)).thenReturn(subscriptionResponse);

        List<SubscriptionResponse> result = subscriptionService.getExpiringSubscriptions(7);

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should check if member has active subscription")
    void hasActiveSubscription_ReturnsTrue() {
        when(subscriptionRepository.existsByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE)).thenReturn(true);

        boolean result = subscriptionService.hasActiveSubscription(memberId);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should return false when member has no active subscription")
    void hasActiveSubscription_ReturnsFalse() {
        when(subscriptionRepository.existsByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE)).thenReturn(false);

        boolean result = subscriptionService.hasActiveSubscription(memberId);

        assertThat(result).isFalse();
    }
}
