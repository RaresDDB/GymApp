package com.gymapp.backend.controllers;

import com.gymapp.backend.dtos.requests.CreateGymClassRequest;
import com.gymapp.backend.dtos.requests.EnrollInClassRequest;
import com.gymapp.backend.dtos.responses.ClassEnrollmentResponse;
import com.gymapp.backend.dtos.responses.GymClassResponse;
import com.gymapp.backend.exceptions.ClassFullException;
import com.gymapp.backend.exceptions.GlobalExceptionHandler;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.enums.ClassType;
import com.gymapp.backend.enums.EnrollmentStatus;
import com.gymapp.backend.services.GymClassService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.test.autoconfigure.json.AutoConfigureJsonTesters;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GymClassController.class)
@Import(GlobalExceptionHandler.class)
@AutoConfigureJsonTesters
class GymClassControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JacksonTester<EnrollInClassRequest> enrollInClassRequestJson;

    @Autowired
    private JacksonTester<CreateGymClassRequest> createGymClassRequestJson;

    @MockitoBean
    @SuppressWarnings("unused")
    private GymClassService classService;

    private UUID classId;

    private UUID memberId;

    private GymClassResponse classResponse;

    private CreateGymClassRequest createRequest;

    @BeforeEach
    void setUp() {
        classId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        classResponse = GymClassResponse.builder()
            .id(classId)
            .name("Morning Yoga")
            .instructor("Jane Doe")
            .maxCapacity(20)
            .currentEnrollment(5)
            .availableSpots(15)
            .scheduledAt(LocalDateTime.now().plusDays(1))
            .durationMinutes(60)
            .classType(ClassType.YOGA)
            .active(true)
            .build();

        createRequest = CreateGymClassRequest.builder()
            .name("Morning Yoga")
            .instructor("Jane Doe")
            .maxCapacity(20)
            .scheduledAt(LocalDateTime.now().plusDays(1))
            .durationMinutes(60)
            .classType(ClassType.YOGA)
            .build();
    }

    @Test
    @DisplayName("POST /api/classes - Should create class successfully")
    void createClass_Success() throws Exception {
        when(classService.createClass(any(CreateGymClassRequest.class))).thenReturn(classResponse);

        mockMvc.perform(
            post("/api/classes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createGymClassRequestJson.write(createRequest).getJson())
        )
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.id").value(classId.toString()))
        .andExpect(jsonPath("$.name").value("Morning Yoga"));
    }

    @Test
    @DisplayName("GET /api/classes/{id} - Should return class by ID")
    void getClassById_Success() throws Exception {
        when(classService.getClassById(classId)).thenReturn(classResponse);

        mockMvc.perform(get("/api/classes/{id}", classId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Morning Yoga"));
    }

    @Test
    @DisplayName("GET /api/classes/{id} - Should return 404 for non-existent class")
    void getClassById_NotFound() throws Exception {
        when(classService.getClassById(classId))
            .thenThrow(new ResourceNotFoundException("GymClass", "id", classId));

        mockMvc.perform(get("/api/classes/{id}", classId))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/classes - Should return paginated classes")
    void getAllClasses_Success() throws Exception {
        Page<GymClassResponse> page = new PageImpl<>(List.of(classResponse));

        when(classService.getAllClasses(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/classes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].name").value("Morning Yoga"));
    }

    @Test
    @DisplayName("GET /api/classes/available - Should return available classes")
    void getAvailableClasses_Success() throws Exception {
        when(classService.getAvailableClasses()).thenReturn(List.of(classResponse));

        mockMvc.perform(get("/api/classes/available"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].availableSpots").value(15));
    }

    @Test
    @DisplayName("GET /api/classes/type/{classType} - Should return classes by type")
    void getClassesByType_Success() throws Exception {
        when(classService.getClassesByType(ClassType.YOGA)).thenReturn(List.of(classResponse));

        mockMvc.perform(get("/api/classes/type/{classType}", ClassType.YOGA))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].classType").value("YOGA"));
    }

    @Test
    @DisplayName("POST /api/classes/{id}/enroll - Should enroll member successfully")
    void enrollMember_Success() throws Exception {
        EnrollInClassRequest enrollRequest = EnrollInClassRequest.builder()
            .memberId(memberId)
            .build();

        ClassEnrollmentResponse enrollmentResponse = ClassEnrollmentResponse.builder()
            .id(UUID.randomUUID())
            .memberId(memberId)
            .memberName("John Doe")
            .gymClassId(classId)
            .gymClassName("Morning Yoga")
            .status(EnrollmentStatus.ENROLLED)
            .build();

        when(classService.enrollMember(eq(classId), eq(memberId))).thenReturn(enrollmentResponse);

        mockMvc.perform(
            post("/api/classes/{id}/enroll", classId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollInClassRequestJson.write(enrollRequest).getJson())
        )
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("ENROLLED"));
    }

    @Test
    @DisplayName("POST /api/classes/{id}/enroll - Should return 400 when class is full")
    void enrollMember_ClassFull() throws Exception {
        EnrollInClassRequest enrollRequest = EnrollInClassRequest.builder()
            .memberId(memberId)
            .build();

        when(classService.enrollMember(eq(classId), eq(memberId)))
            .thenThrow(new ClassFullException("Morning Yoga"));

        mockMvc.perform(
            post("/api/classes/{id}/enroll", classId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(enrollInClassRequestJson.write(enrollRequest).getJson())
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("DELETE /api/classes/{id}/enroll/{memberId} - Should cancel enrollment")
    void cancelEnrollment_Success() throws Exception {
        mockMvc.perform(delete("/api/classes/{id}/enroll/{memberId}", classId, memberId))
            .andExpect(status().isNoContent());
    }
}