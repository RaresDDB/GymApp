package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateGymClassRequest;
import com.gymapp.backend.dtos.responses.ClassEnrollmentResponse;
import com.gymapp.backend.dtos.responses.GymClassResponse;
import com.gymapp.backend.exceptions.ClassFullException;
import com.gymapp.backend.exceptions.DuplicateResourceException;
import com.gymapp.backend.exceptions.ResourceNotFoundException;
import com.gymapp.backend.mappers.ClassEnrollmentMapper;
import com.gymapp.backend.mappers.GymClassMapper;
import com.gymapp.backend.entities.ClassEnrollment;
import com.gymapp.backend.entities.GymClass;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.enums.ClassType;
import com.gymapp.backend.enums.EnrollmentStatus;
import com.gymapp.backend.repositories.ClassEnrollmentRepository;
import com.gymapp.backend.repositories.GymClassRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class GymClassServiceTest {

    @Mock
    private GymClassRepository classRepository;

    @Mock
    private ClassEnrollmentRepository enrollmentRepository;

    @Mock
    private GymClassMapper classMapper;

    @Mock
    private ClassEnrollmentMapper enrollmentMapper;

    @Mock
    private MemberService memberService;

    @Mock
    private SubscriptionService subscriptionService;

    @InjectMocks
    private GymClassService gymClassService;

    private GymClass gymClass;
    private GymClassResponse gymClassResponse;
    private CreateGymClassRequest createRequest;
    private Member member;
    private UUID classId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        classId = UUID.randomUUID();
        memberId = UUID.randomUUID();

        gymClass = GymClass.builder()
                .name("Morning Yoga")
                .description("Relaxing yoga session")
                .instructor("Jane Doe")
                .maxCapacity(20)
                .currentEnrollment(5)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationMinutes(60)
                .classType(ClassType.YOGA)
                .active(true)
                .build();
        gymClass.setId(classId);

        gymClassResponse = GymClassResponse.builder()
                .id(classId)
                .name("Morning Yoga")
                .description("Relaxing yoga session")
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
                .description("Relaxing yoga session")
                .instructor("Jane Doe")
                .maxCapacity(20)
                .scheduledAt(LocalDateTime.now().plusDays(1))
                .durationMinutes(60)
                .classType(ClassType.YOGA)
                .build();

        member = Member.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .dateOfBirth(LocalDate.of(1990, 1, 15))
                .active(true)
                .build();
        member.setId(memberId);
    }

    @Test
    @DisplayName("Should create gym class successfully")
    void createClass_Success() {
        when(classMapper.toEntity(createRequest)).thenReturn(gymClass);
        when(classRepository.save(any(GymClass.class))).thenReturn(gymClass);
        when(classMapper.toResponse(gymClass)).thenReturn(gymClassResponse);

        GymClassResponse result = gymClassService.createClass(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(createRequest.getName());
        verify(classRepository).save(any(GymClass.class));
    }

    @Test
    @DisplayName("Should get class by ID successfully")
    void getClassById_Success() {
        when(classRepository.findById(classId)).thenReturn(Optional.of(gymClass));
        when(classMapper.toResponse(gymClass)).thenReturn(gymClassResponse);

        GymClassResponse result = gymClassService.getClassById(classId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(classId);
    }

    @Test
    @DisplayName("Should throw exception when class not found")
    void getClassById_NotFound() {
        when(classRepository.findById(classId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> gymClassService.getClassById(classId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should get all active classes")
    void getAllActiveClasses_Success() {
        when(classRepository.findByActiveTrue()).thenReturn(List.of(gymClass));
        when(classMapper.toResponse(gymClass)).thenReturn(gymClassResponse);

        List<GymClassResponse> result = gymClassService.getAllActiveClasses();

        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("Should get classes by type")
    void getClassesByType_Success() {
        when(classRepository.findByClassTypeAndActiveTrue(ClassType.YOGA)).thenReturn(List.of(gymClass));
        when(classMapper.toResponse(gymClass)).thenReturn(gymClassResponse);

        List<GymClassResponse> result = gymClassService.getClassesByType(ClassType.YOGA);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getClassType()).isEqualTo(ClassType.YOGA);
    }

    @Test
    @DisplayName("Should enroll member in class successfully")
    void enrollMember_Success() {
        ClassEnrollment enrollment = ClassEnrollment.builder()
                .member(member)
                .gymClass(gymClass)
                .status(EnrollmentStatus.ENROLLED)
                .build();
        enrollment.setId(UUID.randomUUID());

        ClassEnrollmentResponse enrollmentResponse = ClassEnrollmentResponse.builder()
                .id(enrollment.getId())
                .memberId(memberId)
                .memberName("John Doe")
                .gymClassId(classId)
                .gymClassName("Morning Yoga")
                .status(EnrollmentStatus.ENROLLED)
                .build();

        when(classRepository.findById(classId)).thenReturn(Optional.of(gymClass));
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(subscriptionService.hasActiveSubscription(memberId)).thenReturn(true);
        when(enrollmentRepository.existsByMemberIdAndGymClassId(memberId, classId)).thenReturn(false);
        when(enrollmentRepository.save(any(ClassEnrollment.class))).thenReturn(enrollment);
        when(classRepository.save(any(GymClass.class))).thenReturn(gymClass);
        when(enrollmentMapper.toResponse(enrollment)).thenReturn(enrollmentResponse);

        ClassEnrollmentResponse result = gymClassService.enrollMember(classId, memberId);

        assertThat(result).isNotNull();
        assertThat(result.getMemberId()).isEqualTo(memberId);
        verify(enrollmentRepository).save(any(ClassEnrollment.class));
    }

    @Test
    @DisplayName("Should throw exception when class is full")
    void enrollMember_ClassFull() {
        gymClass.setCurrentEnrollment(20);

        when(classRepository.findById(classId)).thenReturn(Optional.of(gymClass));
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(subscriptionService.hasActiveSubscription(memberId)).thenReturn(true);
        when(enrollmentRepository.existsByMemberIdAndGymClassId(memberId, classId)).thenReturn(false);

        assertThatThrownBy(() -> gymClassService.enrollMember(classId, memberId))
                .isInstanceOf(ClassFullException.class);
    }

    @Test
    @DisplayName("Should throw exception when already enrolled")
    void enrollMember_AlreadyEnrolled() {
        when(classRepository.findById(classId)).thenReturn(Optional.of(gymClass));
        when(memberService.findMemberById(memberId)).thenReturn(member);
        when(subscriptionService.hasActiveSubscription(memberId)).thenReturn(true);
        when(enrollmentRepository.existsByMemberIdAndGymClassId(memberId, classId)).thenReturn(true);

        assertThatThrownBy(() -> gymClassService.enrollMember(classId, memberId))
                .isInstanceOf(DuplicateResourceException.class);
    }
}
