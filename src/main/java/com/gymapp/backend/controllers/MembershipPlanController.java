package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateMembershipPlanRequest;
import com.gymapp.backend.dtos.requests.UpdateMembershipPlanRequest;
import com.gymapp.backend.dtos.responses.MembershipPlanResponse;
import com.gymapp.backend.services.MembershipPlanService;
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
@RequestMapping("/api/plans")
@RequiredArgsConstructor
@Tag(name = "Membership Plans", description = "Membership plan management endpoints")
public class MembershipPlanController {
    private final MembershipPlanService planService;

    @PostMapping
    @Operation(summary = "Create a new membership plan", description = "Creates a new membership plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Plan created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Plan name already exists")
    })
    public ResponseEntity<MembershipPlanResponse> createPlan(
            @Valid @RequestBody CreateMembershipPlanRequest request) {
        MembershipPlanResponse response = planService.createPlan(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get plan by ID", description = "Retrieves a membership plan by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan found"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    public ResponseEntity<MembershipPlanResponse> getPlanById(
            @Parameter(description = "Plan UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(planService.getPlanById(id));
    }

    @GetMapping
    @Operation(summary = "Get all active plans", description = "Retrieves all active membership plans")
    public ResponseEntity<List<MembershipPlanResponse>> getAllActivePlans() {
        return ResponseEntity.ok(planService.getAllActivePlans());
    }

    @GetMapping("/all")
    @Operation(summary = "Get all plans", description = "Retrieves all membership plans including inactive")
    public ResponseEntity<List<MembershipPlanResponse>> getAllPlans() {
        return ResponseEntity.ok(planService.getAllPlans());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update plan", description = "Updates an existing membership plan")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Plan updated successfully"),
            @ApiResponse(responseCode = "404", description = "Plan not found"),
            @ApiResponse(responseCode = "409", description = "Plan name already exists")
    })
    public ResponseEntity<MembershipPlanResponse> updatePlan(
            @Parameter(description = "Plan UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateMembershipPlanRequest request) {
        return ResponseEntity.ok(planService.updatePlan(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete plan", description = "Soft deletes a membership plan (deactivates)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Plan deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Plan not found")
    })
    public ResponseEntity<Void> deletePlan(
            @Parameter(description = "Plan UUID") @PathVariable UUID id) {
        planService.deletePlan(id);
        return ResponseEntity.noContent().build();
    }
}
