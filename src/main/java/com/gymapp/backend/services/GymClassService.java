package com.gymapp.backend.services;

import com.gymapp.backend.dtos.requests.CreateGymClassRequest;
import com.gymapp.backend.dtos.requests.UpdateGymClassRequest;
import com.gymapp.backend.dtos.responses.ClassEnrollmentResponse;
import com.gymapp.backend.dtos.responses.GymClassResponse;
import com.gymapp.backend.exceptions.*;
import com.gymapp.backend.mappers.ClassEnrollmentMapper;
import com.gymapp.backend.mappers.GymClassMapper;
import com.gymapp.backend.entities.ClassEnrollment;
import com.gymapp.backend.entities.GymClass;
import com.gymapp.backend.entities.Member;
import com.gymapp.backend.enums.ClassType;
import com.gymapp.backend.enums.EnrollmentStatus;
import com.gymapp.backend.repositories.ClassEnrollmentRepository;
import com.gymapp.backend.repositories.GymClassRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GymClassService {
    private final GymClassRepository classRepository;
    private final ClassEnrollmentRepository enrollmentRepository;
    private final GymClassMapper classMapper;
    private final ClassEnrollmentMapper enrollmentMapper;
    private final MemberService memberService;
    private final SubscriptionService subscriptionService;

    @Transactional
    public GymClassResponse createClass(CreateGymClassRequest request) {
        log.info("Creating new gym class: {}", request.getName());

        GymClass gymClass = classMapper.toEntity(request);
        GymClass savedClass = classRepository.save(gymClass);

        log.info("Gym class created with ID: {}", savedClass.getId());
        return classMapper.toResponse(savedClass);
    }

    @Transactional(readOnly = true)
    public GymClassResponse getClassById(UUID id) {
        log.debug("Fetching gym class with ID: {}", id);
        GymClass gymClass = findClassById(id);
        return classMapper.toResponse(gymClass);
    }

    @Transactional(readOnly = true)
    public List<GymClassResponse> getAllActiveClasses() {
        log.debug("Fetching all active gym classes");
        return classRepository.findByActiveTrue().stream()
                .map(classMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<GymClassResponse> getAllClasses(Pageable pageable) {
        log.debug("Fetching all gym classes with pagination");
        return classRepository.findByActiveTrue(pageable)
                .map(classMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public List<GymClassResponse> getClassesByType(ClassType classType) {
        log.debug("Fetching gym classes by type: {}", classType);
        return classRepository.findByClassTypeAndActiveTrue(classType).stream()
                .map(classMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<GymClassResponse> getAvailableClasses() {
        log.debug("Fetching available gym classes");
        return classRepository.findAvailableClasses(LocalDateTime.now()).stream()
                .map(classMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<GymClassResponse> searchClasses(String search, Pageable pageable) {
        log.debug("Searching gym classes with query: {}", search);
        return classRepository.searchClasses(search, pageable)
                .map(classMapper::toResponse);
    }

    @Transactional
    public GymClassResponse updateClass(UUID id, UpdateGymClassRequest request) {
        log.info("Updating gym class with ID: {}", id);

        GymClass gymClass = findClassById(id);
        classMapper.updateEntity(request, gymClass);
        GymClass updatedClass = classRepository.save(gymClass);

        log.info("Gym class updated with ID: {}", updatedClass.getId());
        return classMapper.toResponse(updatedClass);
    }

    @Transactional
    public void deleteClass(UUID id) {
        log.info("Deleting gym class with ID: {}", id);
        GymClass gymClass = findClassById(id);
        gymClass.setActive(false);
        classRepository.save(gymClass);
        log.info("Gym class deactivated with ID: {}", id);
    }

    @Transactional
    public ClassEnrollmentResponse enrollMember(UUID classId, UUID memberId) {
        log.info("Enrolling member {} in class {}", memberId, classId);

        GymClass gymClass = findClassById(classId);
        Member member = memberService.findMemberById(memberId);

        if (!subscriptionService.hasActiveSubscription(memberId)) {
            throw new MembershipExpiredException(member.getFullName());
        }

        if (enrollmentRepository.existsByMemberIdAndGymClassId(memberId, classId)) {
            throw new DuplicateResourceException("Member is already enrolled in this class");
        }

        if (gymClass.getCurrentEnrollment() >= gymClass.getMaxCapacity()) {
            throw new ClassFullException(gymClass.getName(), gymClass.getMaxCapacity());
        }

        ClassEnrollment enrollment = ClassEnrollment.builder()
                .member(member)
                .gymClass(gymClass)
                .status(EnrollmentStatus.ENROLLED)
                .build();

        ClassEnrollment savedEnrollment = enrollmentRepository.save(enrollment);

        gymClass.setCurrentEnrollment(gymClass.getCurrentEnrollment() + 1);
        classRepository.save(gymClass);

        log.info("Member enrolled successfully");
        return enrollmentMapper.toResponse(savedEnrollment);
    }

    @Transactional
    public void cancelEnrollment(UUID classId, UUID memberId) {
        log.info("Cancelling enrollment for member {} in class {}", memberId, classId);

        GymClass gymClass = findClassById(classId);
        ClassEnrollment enrollment = enrollmentRepository.findByMemberIdAndGymClassId(memberId, classId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (gymClass.getScheduledAt().minusHours(24).isBefore(LocalDateTime.now())) {
            throw new CancellationNotAllowedException();
        }

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        enrollmentRepository.save(enrollment);

        gymClass.setCurrentEnrollment(gymClass.getCurrentEnrollment() - 1);
        classRepository.save(gymClass);

        log.info("Enrollment cancelled successfully");
    }

    @Transactional(readOnly = true)
    public List<ClassEnrollmentResponse> getClassEnrollments(UUID classId) {
        log.debug("Fetching enrollments for class: {}", classId);
        return enrollmentRepository.findByGymClassIdAndStatus(classId, EnrollmentStatus.ENROLLED).stream()
                .map(enrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ClassEnrollmentResponse> getMemberEnrollments(UUID memberId) {
        log.debug("Fetching enrollments for member: {}", memberId);
        return enrollmentRepository.findActiveEnrollmentsByMember(memberId).stream()
                .map(enrollmentMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GymClass findClassById(UUID id) {
        return classRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("GymClass", "id", id));
    }
}
