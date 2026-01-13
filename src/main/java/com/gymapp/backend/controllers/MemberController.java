package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateMemberRequest;
import com.gymapp.backend.dtos.requests.UpdateMemberRequest;
import com.gymapp.backend.dtos.responses.MemberResponse;
import com.gymapp.backend.services.MemberService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
@Tag(name = "Members", description = "Member management endpoints")
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    @Operation(summary = "Register a new member", description = "Creates a new gym member account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Member created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<MemberResponse> createMember(
            @Valid @RequestBody CreateMemberRequest request) {
        MemberResponse response = memberService.createMember(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by ID", description = "Retrieves a member's details by their UUID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member found"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<MemberResponse> getMemberById(
            @Parameter(description = "Member UUID") @PathVariable UUID id) {
        return ResponseEntity.ok(memberService.getMemberById(id));
    }

    @GetMapping
    @Operation(summary = "Get all members", description = "Retrieves all active members with pagination")
    public ResponseEntity<Page<MemberResponse>> getAllMembers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(memberService.getAllMembers(pageable));
    }

    @GetMapping("/search")
    @Operation(summary = "Search members", description = "Search members by name or email")
    public ResponseEntity<Page<MemberResponse>> searchMembers(
            @Parameter(description = "Search query") @RequestParam String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(memberService.searchMembers(query, pageable));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update member", description = "Updates an existing member's information")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Member updated successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ResponseEntity<MemberResponse> updateMember(
            @Parameter(description = "Member UUID") @PathVariable UUID id,
            @Valid @RequestBody UpdateMemberRequest request) {
        return ResponseEntity.ok(memberService.updateMember(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete member", description = "Soft deletes a member (deactivates)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Member deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Member not found")
    })
    public ResponseEntity<Void> deleteMember(
            @Parameter(description = "Member UUID") @PathVariable UUID id) {
        memberService.deleteMember(id);
        return ResponseEntity.noContent().build();
    }
}
