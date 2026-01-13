package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateSubscriptionRequest;
import com.gymapp.backend.dtos.responses.SubscriptionResponse;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.InvalidOperationException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.SubscriptionMapper;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.entities.MembershipPlan;
import com.gymapp.backend.entities.Subscription;
import com.gymapp.backend.enums.SubscriptionStatus;
import com.gymapp.backend.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {
    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionMapper subscriptionMapper;
    private final MemberService memberService;
    private final MembershipPlanService planService;

    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        log.info("Creating subscription for member: {}", request.getMemberId());

        Member member = memberService.findMemberById(request.getMemberId());
        MembershipPlan plan = planService.findPlanById(request.getPlanId());

        if (subscriptionRepository.existsByMemberIdAndStatus(request.getMemberId(), SubscriptionStatus.ACTIVE)) {
            throw new DuplicateResourceException("Member already has an active subscription");
        }

        LocalDate startDate = request.getStartDate() != null ? request.getStartDate() : LocalDate.now();
        LocalDate endDate = startDate.plusMonths(plan.getDurationMonths());

        Subscription subscription = Subscription.builder()
                .member(member)
                .membershipPlan(plan)
                .startDate(startDate)
                .endDate(endDate)
                .status(SubscriptionStatus.ACTIVE)
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription created with ID: {}", savedSubscription.getId());
        return subscriptionMapper.toResponse(savedSubscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getSubscriptionById(UUID id) {
        log.debug("Fetching subscription with ID: {}", id);
        Subscription subscription = findSubscriptionById(id);
        return subscriptionMapper.toResponse(subscription);
    }

    @Transactional(readOnly = true)
    public SubscriptionResponse getMemberSubscription(UUID memberId) {
        log.debug("Fetching subscription for member: {}", memberId);
        Subscription subscription = subscriptionRepository.findByMemberId(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "memberId", memberId));
        return subscriptionMapper.toResponse(subscription);
    }

    @Transactional
    public SubscriptionResponse cancelSubscription(UUID id) {
        log.info("Cancelling subscription with ID: {}", id);

        Subscription subscription = findSubscriptionById(id);

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new InvalidOperationException("Only active subscriptions can be cancelled");
        }

        subscription.setStatus(SubscriptionStatus.CANCELLED);
        Subscription updatedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription cancelled with ID: {}", id);
        return subscriptionMapper.toResponse(updatedSubscription);
    }

    @Transactional
    public SubscriptionResponse renewSubscription(UUID id, UUID newPlanId) {
        log.info("Renewing subscription with ID: {}", id);

        Subscription subscription = findSubscriptionById(id);
        MembershipPlan plan = newPlanId != null ? planService.findPlanById(newPlanId) : subscription.getMembershipPlan();

        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusMonths(plan.getDurationMonths());

        subscription.setMembershipPlan(plan);
        subscription.setStartDate(startDate);
        subscription.setEndDate(endDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        Subscription renewedSubscription = subscriptionRepository.save(subscription);

        log.info("Subscription renewed with ID: {}", id);
        return subscriptionMapper.toResponse(renewedSubscription);
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getExpiringSubscriptions(int daysAhead) {
        log.debug("Fetching subscriptions expiring within {} days", daysAhead);
        LocalDate today = LocalDate.now();
        LocalDate endDate = today.plusDays(daysAhead);

        return subscriptionRepository.findSubscriptionsExpiringBetween(today, endDate).stream()
                .map(subscriptionMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public boolean hasActiveSubscription(UUID memberId) {
        return subscriptionRepository.existsByMemberIdAndStatus(memberId, SubscriptionStatus.ACTIVE);
    }

    @Transactional(readOnly = true)
    public Subscription findSubscriptionById(UUID id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subscription", "id", id));
    }
}
