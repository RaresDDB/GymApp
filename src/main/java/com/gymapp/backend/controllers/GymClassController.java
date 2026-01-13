package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateGymClassRequest;
import com.gymapp.backend.dtos.requests.EnrollInClassRequest;
import com.gymapp.backend.dtos.requests.UpdateGymClassRequest;
import com.gymapp.backend.dtos.responses.ClassEnrollmentResponse;
import com.gymapp.backend.dtos.responses.GymClassResponse;
import com.gymapp.backend.enums.ClassType;
import com.gymapp.backend.services.GymClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
@Tag(name = "Gym Classes", description = "Group fitness class management endpoints")
public class GymClassController {
    private final GymClassService classService;

    @PostMapping
    @Operation(summary = "Create a new gym class", description = "Creates a new group fitness class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Class created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<GymClassResponse> createClass(@Valid @RequestBody CreateGymClassRequest request) {
        GymClassResponse response = classService.createClass(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get class by ID", description = "Retrieves a gym class by its UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Class found"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<GymClassResponse> getClassById(
            @Parameter(description = "Class UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(classService.getClassById(id));
    }

    @GetMapping
    @Operation(summary = "Get all classes", description = "Retrieves all active gym classes with pagination")
    public ResponseEntity<Page<GymClassResponse>> getAllClasses(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(classService.getAllClasses(pageable));
    }

    @GetMapping("/list")
    @Operation(summary = "Get all active classes", description = "Retrieves all active gym classes as a list")
    public ResponseEntity<List<GymClassResponse>> getAllActiveClasses() {
        return ResponseEntity.ok(classService.getAllActiveClasses());
    }

    @GetMapping("/available")
    @Operation(summary = "Get available classes", description = "Retrieves classes with available spots")
    public ResponseEntity<List<GymClassResponse>> getAvailableClasses() {
        return ResponseEntity.ok(classService.getAvailableClasses());
    }

    @GetMapping("/type/{classType}")
    @Operation(summary = "Get classes by type", description = "Retrieves classes of a specific type")
    public ResponseEntity<List<GymClassResponse>> getClassesByType(
            @Parameter(description = "Class type") @PathVariable ClassType classType) {
        return ResponseEntity.ok(classService.getClassesByType(classType));
    }

    @GetMapping("/search")
    @Operation(summary = "Search classes", description = "Search classes by name or instructor")
    public ResponseEntity<Page<GymClassResponse>> searchClasses(
            @Parameter(description = "Search query") @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(classService.searchClasses(query, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update class", description = "Updates an existing gym class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Class updated successfully"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<GymClassResponse> updateClass(
            @Parameter(description = "Class UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateGymClassRequest request) {
        return ResponseEntity.ok(classService.updateClass(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete class", description = "Soft deletes a gym class (deactivates)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Class deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Class not found")
    })
    public ResponseEntity<Void> deleteClass(
            @Parameter(description = "Class UUID") @PathVariable UUID id) {
        classService.deleteClass(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/enroll")
    @Operation(summary = "Enroll in class", description = "Enrolls a member in a gym class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Enrolled successfully"),
            @ApiResponse(responseCode = "400", description = "Class is full or enrollment failed"),
            @ApiResponse(responseCode = "403", description = "Member's membership has expired"),
            @ApiResponse(responseCode = "404", description = "Class or Member not found"),
            @ApiResponse(responseCode = "409", description = "Member already enrolled")
    })
    public ResponseEntity<ClassEnrollmentResponse> enrollMember(
            @Parameter(description = "Class UUID") @PathVariable UUID id,
            @Valid @RequestBody EnrollInClassRequest request) {
        ClassEnrollmentResponse response = classService.enrollMember(id, request.getMemberId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}/enroll/{memberId}")
    @Operation(summary = "Cancel enrollment", description = "Cancels a member's enrollment in a class")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Enrollment cancelled"),
            @ApiResponse(responseCode = "400", description = "Cancellation not allowed"),
            @ApiResponse(responseCode = "404", description = "Enrollment not found")
    })
    public ResponseEntity<Void> cancelEnrollment(
            @Parameter(description = "Class UUID") @PathVariable UUID id,
            @Parameter(description = "Member UUID") @PathVariable UUID memberId) {
        classService.cancelEnrollment(id, memberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/enrollments")
    @Operation(summary = "Get class enrollments", description = "Retrieves all enrollments for a class")
    public ResponseEntity<List<ClassEnrollmentResponse>> getClassEnrollments(
            @Parameter(description = "Class UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(classService.getClassEnrollments(id));
    }

    @GetMapping("/member/{memberId}/enrollments")
    @Operation(summary = "Get member's enrollments", description = "Retrieves all enrollments for a member")
    public ResponseEntity<List<ClassEnrollmentResponse>> getMemberEnrollments(
            @Parameter(description = "Member UUID") @PathVariable UUID memberId) {
        return ResponseEntity.ok(classService.getMemberEnrollments(memberId));
    }
}
