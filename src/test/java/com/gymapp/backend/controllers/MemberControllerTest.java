package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateMemberRequest;
import com.gymapp.backend.dtos.requests.UpdateMemberRequest;
import com.gymapp.backend.dtos.responses.MemberResponse;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.GlobalExceptionHandler;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.services.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MemberController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureJsonTesters
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<CreateMemberRequest> createMemberRequestJson;

    @Autowired
    private JacksonTester<UpdateMemberRequest> updateMemberRequestJson;

    @MockitoBean
    @SuppressWarnings("unused")
    private MemberService memberService;

    private UUID memberId;

    private MemberResponse memberResponse;

    private CreateMemberRequest createRequest;


    @BeforeEach
    void setUp() {
        memberId = UUID.randomUUID();

        memberResponse = MemberResponse.builder()
            .id(memberId)
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("+1234567890")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .active(true)
            .build();

        createRequest = CreateMemberRequest.builder()
            .firstName("John")
            .lastName("Doe")
            .email("john.doe@example.com")
            .phone("+1234567890")
            .dateOfBirth(LocalDate.of(1990, 1, 15))
            .build();
    }

    @Test
    @DisplayName("POST /api/members - Should create member successfully")
    void createMember_Success() throws Exception {
        when(memberService.createMember(any(CreateMemberRequest.class))).thenReturn(memberResponse);

        mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMemberRequestJson.write(createRequest).getJson())
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(memberId.toString()))
            .andExpect(jsonPath("$.email").value("john.doe@example.com"));
    }

    @Test
    @DisplayName("POST /api/members - Should return 400 for invalid request")
    void createMember_InvalidRequest() throws Exception {
        CreateMemberRequest invalidRequest = CreateMemberRequest.builder()
            .firstName("")
            .email("invalid-email")
            .build();

        mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMemberRequestJson.write(invalidRequest).getJson())
        )
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/members - Should return 409 for duplicate email")
    void createMember_DuplicateEmail() throws Exception {
        when(memberService.createMember(any(CreateMemberRequest.class)))
            .thenThrow(new DuplicateResourceException("Member", "email", "john.doe@example.com"));

        mockMvc.perform(
            post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMemberRequestJson.write(createRequest).getJson())
        )
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("GET /api/members/{id} - Should return member by ID")
    void getMemberById_Success() throws Exception {
        when(memberService.getMemberById(memberId)).thenReturn(memberResponse);

        mockMvc.perform(get("/api/members/{id}", memberId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(memberId.toString()))
            .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @DisplayName("GET /api/members/{id} - Should return 404 for non-existent member")
    void getMemberById_NotFound() throws Exception {
        when(memberService.getMemberById(memberId))
            .thenThrow(new ResourceNotFoundException("Member", "id", memberId));

        mockMvc.perform(get("/api/members/{id}", memberId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/members - Should return paginated members")
    void getAllMembers_Success() throws Exception {
        Page<MemberResponse> page = new PageImpl<>(List.of(memberResponse));

        when(memberService.getAllMembers(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/members"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(memberId.toString()));
    }

    @Test
    @DisplayName("GET /api/members/search - Should search members")
    void searchMembers_Success() throws Exception {
        Page<MemberResponse> page = new PageImpl<>(List.of(memberResponse));

        when(memberService.searchMembers(eq("John"), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/members/search").param("query", "John"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].firstName").value("John"));
    }

    @Test
    @DisplayName("PUT /api/members/{id} - Should update member successfully")
    void updateMember_Success() throws Exception {
        UpdateMemberRequest updateRequest = UpdateMemberRequest.builder()
            .firstName("Jane")
            .build();

        when(memberService.updateMember(eq(memberId), any(UpdateMemberRequest.class)))
            .thenReturn(memberResponse);

        mockMvc.perform(
            put("/api/members/{id}", memberId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateMemberRequestJson.write(updateRequest).getJson())
        )
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/members/{id} - Should delete member successfully")
    void deleteMember_Success() throws Exception {
        doNothing().when(memberService).deleteMember(memberId);

        mockMvc.perform(delete("/api/members/{id}", memberId))
            .andExpect(status().isNoContent());
    }
}