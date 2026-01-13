package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateMembershipPlanRequest;
import com.gymapp.backend.dtos.responses.MembershipPlanResponse;
import com.gymapp.backend.exceptions.GlobalExceptionHandler;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.services.MembershipPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MembershipPlanController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureJsonTesters
class MembershipPlanControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<CreateMembershipPlanRequest> createMembershipPlanRequestJson;

    @MockitoBean
    @SuppressWarnings("unused")
    private MembershipPlanService planService;

    private UUID planId;

    private MembershipPlanResponse planResponse;

    private CreateMembershipPlanRequest createRequest;


    @BeforeEach
    void setUp() {
        planId = UUID.randomUUID();

        planResponse = MembershipPlanResponse.builder()
            .id(planId)
            .name("Premium")
            .description("Premium membership")
            .price(BigDecimal.valueOf(99.99))
            .durationMonths(12)
            .includesPersonalTraining(true)
            .includesGroupClasses(true)
            .active(true)
            .build();

        createRequest = CreateMembershipPlanRequest.builder()
            .name("Premium")
            .description("Premium membership")
            .price(BigDecimal.valueOf(99.99))
            .durationMonths(12)
            .includesPersonalTraining(true)
            .includesGroupClasses(true)
            .build();
    }

    @Test
    @DisplayName("POST /api/plans - Should create plan successfully")
    void createPlan_Success() throws Exception {
        when(planService.createPlan(any(CreateMembershipPlanRequest.class))).thenReturn(planResponse);

        mockMvc.perform(
            post("/api/plans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createMembershipPlanRequestJson.write(createRequest).getJson())
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(planId.toString()))
            .andExpect(jsonPath("$.name").value("Premium"));
    }

    @Test
    @DisplayName("GET /api/plans/{id} - Should return plan by ID")
    void getPlanById_Success() throws Exception {
        when(planService.getPlanById(planId)).thenReturn(planResponse);

        mockMvc.perform(get("/api/plans/{id}", planId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Premium"));
    }

    @Test
    @DisplayName("GET /api/plans/{id} - Should return 404 for non-existent plan")
    void getPlanById_NotFound() throws Exception {
        when(planService.getPlanById(planId))
            .thenThrow(new ResourceNotFoundException("MembershipPlan", "id", planId));

        mockMvc.perform(get("/api/plans/{id}", planId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/plans - Should return all active plans")
    void getAllActivePlans_Success() throws Exception {
        when(planService.getAllActivePlans()).thenReturn(List.of(planResponse));

        mockMvc.perform(get("/api/plans"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].name").value("Premium"));
    }

    @Test
    @DisplayName("DELETE /api/plans/{id} - Should delete plan successfully")
    void deletePlan_Success() throws Exception {
        mockMvc.perform(delete("/api/plans/{id}", planId))
            .andExpect(status().isNoContent());
    }
}