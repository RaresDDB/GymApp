package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateSubscriptionRequest;
import com.gymapp.backend.dtos.responses.SubscriptionResponse;
import com.gymapp.backend.enums.SubscriptionStatus;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.GlobalExceptionHandler;
import com.gymapp.backend.exceptions.InvalidOperationException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.services.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SubscriptionController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureJsonTesters
class SubscriptionControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<CreateSubscriptionRequest> createSubscriptionRequestJson;

    @MockitoBean
    @SuppressWarnings("unused")
    private SubscriptionService subscriptionService;
    private UUID subscriptionId;
    private UUID memberId;
    private UUID planId;
    private SubscriptionResponse subscriptionResponse;
    private CreateSubscriptionRequest createRequest;

    @BeforeEach
    void setUp() {
        subscriptionId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        planId = UUID.randomUUID();

        subscriptionResponse = SubscriptionResponse.builder()
            .id(subscriptionId)
            .memberId(memberId)
            .memberName("John Doe")
            .planId(planId)
            .planName("Premium")
            .startDate(LocalDate.now())
            .endDate(LocalDate.now().plusMonths(3))
            .status(SubscriptionStatus.ACTIVE)
            .createdAt(LocalDateTime.now())
            .build();

        createRequest = CreateSubscriptionRequest.builder()
            .memberId(memberId)
            .planId(planId)
            .startDate(LocalDate.now())
            .build();
    }

    @Test
    @DisplayName("POST /api/subscriptions - Should create subscription successfully")
    void createSubscription_Success() throws Exception {
        when(subscriptionService.createSubscription(any(CreateSubscriptionRequest.class)))
            .thenReturn(subscriptionResponse);

        mockMvc.perform(
            post("/api/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createSubscriptionRequestJson.write(createRequest).getJson())
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
            .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("POST /api/subscriptions - Should return 400 for invalid request")
    void createSubscription_InvalidRequest() throws Exception {
        CreateSubscriptionRequest invalidRequest = CreateSubscriptionRequest.builder()
                .memberId(null)
                .planId(null)
                .build();

        mockMvc.perform(
            post("/api/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createSubscriptionRequestJson.write(invalidRequest).getJson())
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/subscriptions - Should return 409 for duplicate subscription")
    void createSubscription_DuplicateSubscription() throws Exception {
        when(subscriptionService.createSubscription(any(CreateSubscriptionRequest.class)))
                .thenThrow(new DuplicateResourceException("Member already has an active subscription"));

        mockMvc.perform(
            post("/api/subscriptions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(createSubscriptionRequestJson.write(createRequest).getJson())
        )
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/subscriptions/{id} - Should return subscription by ID")
    void getSubscriptionById_Success() throws Exception {
        when(subscriptionService.getSubscriptionById(subscriptionId)).thenReturn(subscriptionResponse);

        mockMvc.perform(get("/api/subscriptions/{id}", subscriptionId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(subscriptionId.toString()))
            .andExpect(jsonPath("$.memberName").value("John Doe"));
    }

    @Test
    @DisplayName("GET /api/subscriptions/{id} - Should return 404 for non-existent subscription")
    void getSubscriptionById_NotFound() throws Exception {
        when(subscriptionService.getSubscriptionById(subscriptionId))
            .thenThrow(new ResourceNotFoundException("Subscription", "id", subscriptionId));

        mockMvc.perform(get("/api/subscriptions/{id}", subscriptionId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/subscriptions/member/{memberId} - Should return member's subscription")
    void getMemberSubscription_Success() throws Exception {
        when(subscriptionService.getMemberSubscription(memberId)).thenReturn(subscriptionResponse);

        mockMvc.perform(get("/api/subscriptions/member/{memberId}", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.memberId").value(memberId.toString()));
    }

    @Test
    @DisplayName("GET /api/subscriptions/member/{memberId} - Should return 404 when no subscription")
    void getMemberSubscription_NotFound() throws Exception {
        when(subscriptionService.getMemberSubscription(memberId))
            .thenThrow(new ResourceNotFoundException("Subscription", "memberId", memberId));

        mockMvc.perform(get("/api/subscriptions/member/{memberId}", memberId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/subscriptions/{id}/cancel - Should cancel subscription")
    void cancelSubscription_Success() throws Exception {
        SubscriptionResponse cancelledResponse = SubscriptionResponse.builder()
                .id(subscriptionId)
                .status(SubscriptionStatus.CANCELLED)
                .build();

        when(subscriptionService.cancelSubscription(subscriptionId)).thenReturn(cancelledResponse);

        mockMvc.perform(put("/api/subscriptions/{id}/cancel", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @DisplayName("PUT /api/subscriptions/{id}/cancel - Should return 400 for non-active subscription")
    void cancelSubscription_NotActive() throws Exception {
        when(subscriptionService.cancelSubscription(subscriptionId))
                .thenThrow(new InvalidOperationException("Only active subscriptions can be cancelled"));

        mockMvc.perform(put("/api/subscriptions/{id}/cancel", subscriptionId))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/subscriptions/{id}/renew - Should renew subscription")
    void renewSubscription_Success() throws Exception {
        when(subscriptionService.renewSubscription(eq(subscriptionId), any())).thenReturn(subscriptionResponse);

        mockMvc.perform(put("/api/subscriptions/{id}/renew", subscriptionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("PUT /api/subscriptions/{id}/renew - Should renew with new plan")
    void renewSubscription_WithNewPlan() throws Exception {
        UUID newPlanId = UUID.randomUUID();
        when(subscriptionService.renewSubscription(subscriptionId, newPlanId)).thenReturn(subscriptionResponse);

        mockMvc.perform(put("/api/subscriptions/{id}/renew", subscriptionId)
                        .param("planId", newPlanId.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/subscriptions/expiring - Should return expiring subscriptions")
    void getExpiringSubscriptions_Success() throws Exception {
        when(subscriptionService.getExpiringSubscriptions(7)).thenReturn(List.of(subscriptionResponse));

        mockMvc.perform(get("/api/subscriptions/expiring").param("daysAhead", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(subscriptionId.toString()));
    }

    @Test
    @DisplayName("GET /api/subscriptions/expiring - Should use default days ahead")
    void getExpiringSubscriptions_DefaultDays() throws Exception {
        when(subscriptionService.getExpiringSubscriptions(7)).thenReturn(List.of(subscriptionResponse));

        mockMvc.perform(get("/api/subscriptions/expiring"))
                .andExpect(status().isOk());
    }
}
