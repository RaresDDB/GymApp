package com.gymapp.backend.repositories;

import com.gymapp.backend.entities.GymClass;
import com.gymapp.backend.enums.ClassType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface GymClassRepository extends JpaRepository<GymClass, UUID> {
    List<GymClass> findByActiveTrue();

    Page<GymClass> findByActiveTrue(Pageable pageable);

    List<GymClass> findByClassTypeAndActiveTrue(ClassType classType);

    @Query("SELECT gc FROM GymClass gc WHERE gc.active = true AND gc.scheduledAt >= :start AND gc.scheduledAt <= :end")
    List<GymClass> findClassesBetweenDates(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT gc FROM GymClass gc WHERE gc.active = true AND gc.currentEnrollment < gc.maxCapacity AND gc.scheduledAt > :now")
    List<GymClass> findAvailableClasses(@Param("now") LocalDateTime now);

    @Query("SELECT gc FROM GymClass gc WHERE gc.active = true AND " +
            "(LOWER(gc.name) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(gc.instructor) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<GymClass> searchClasses(@Param("search") String search, Pageable pageable);

    List<GymClass> findByInstructorIgnoreCaseAndActiveTrue(String instructor);
}
