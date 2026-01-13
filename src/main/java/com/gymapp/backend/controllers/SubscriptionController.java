package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateSubscriptionRequest;
import com.gymapp.backend.dtos.responses.SubscriptionResponse;
import com.gymapp.backend.services.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "Subscription management endpoints")
public class SubscriptionController {
    private final SubscriptionService subscriptionService;

    @PostMapping
    @Operation(summary = "Create subscription", description = "Creates a new subscription for a member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Subscription created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "404", description = "Member or Plan not found"),
            @ApiResponse(responseCode = "409", description = "Member already has active subscription")
    })
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get subscription by ID", description = "Retrieves a subscription by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription found"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(
            @Parameter(description = "Subscription UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.getSubscriptionById(id));
    }

    @GetMapping("/member/{memberId}")
    @Operation(summary = "Get member's subscription", description = "Retrieves the subscription for a specific member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription found"),
            @ApiResponse(responseCode = "404", description = "Subscription not found for member")
    })
    public ResponseEntity<SubscriptionResponse> getMemberSubscription(
            @Parameter(description = "Member UUID") @PathVariable UUID memberId) {
        return ResponseEntity.ok(subscriptionService.getMemberSubscription(memberId));
    }

    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel subscription", description = "Cancels an active subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription cancelled"),
            @ApiResponse(responseCode = "400", description = "Subscription cannot be cancelled"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<SubscriptionResponse> cancelSubscription(
            @Parameter(description = "Subscription UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(subscriptionService.cancelSubscription(id));
    }

    @PutMapping("/{id}/renew")
    @Operation(summary = "Renew subscription", description = "Renews an existing subscription")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription renewed"),
            @ApiResponse(responseCode = "404", description = "Subscription not found")
    })
    public ResponseEntity<SubscriptionResponse> renewSubscription(
            @Parameter(description = "Subscription UUID") @PathVariable UUID id,
            @Parameter(description = "New Plan UUID (optional)") @RequestParam(required = false) UUID planId) {
        return ResponseEntity.ok(subscriptionService.renewSubscription(id, planId));
    }

    @GetMapping("/expiring")
    @Operation(summary = "Get expiring subscriptions", description = "Retrieves subscriptions expiring within specified days")
    public ResponseEntity<List<SubscriptionResponse>> getExpiringSubscriptions(
            @Parameter(description = "Days ahead to check") @RequestParam(defaultValue = "7") int daysAhead) {
        return ResponseEntity.ok(subscriptionService.getExpiringSubscriptions(daysAhead));
    }
}
