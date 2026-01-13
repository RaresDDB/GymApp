package com.gymapp.backend.repositories;

import com.gymapp.backend.entities.ClassEnrollment;
import com.gymapp.backend.enums.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, UUID> {
    List<ClassEnrollment> findByMemberId(UUID memberId);

    List<ClassEnrollment> findByGymClassId(UUID gymClassId);

    Optional<ClassEnrollment> findByMemberIdAndGymClassId(UUID memberId, UUID gymClassId);

    boolean existsByMemberIdAndGymClassId(UUID memberId, UUID gymClassId);

    @Query("SELECT COUNT(ce) FROM ClassEnrollment ce WHERE ce.gymClass.id = :classId AND ce.status = 'ENROLLED'")
    int countActiveEnrollments(@Param("classId") UUID classId);

    List<ClassEnrollment> findByGymClassIdAndStatus(UUID gymClassId, EnrollmentStatus status);

    @Query("SELECT ce FROM ClassEnrollment ce WHERE ce.member.id = :memberId AND ce.status = 'ENROLLED'")
    List<ClassEnrollment> findActiveEnrollmentsByMember(@Param("memberId") UUID memberId);
}
