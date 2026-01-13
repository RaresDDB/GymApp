package com.gymapp.backend.repositories;

import com.gymapp.backend.entities.Trainer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, UUID> {
    Optional<Trainer> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Trainer> findByActiveTrue();

    Page<Trainer> findByActiveTrue(Pageable pageable);

    List<Trainer> findBySpecializationIgnoreCaseAndActiveTrue(String specialization);

    @Query("SELECT t FROM Trainer t WHERE t.active = true AND " +
            "(LOWER(t.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(t.specialization) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Trainer> searchTrainers(@Param("search") String search, Pageable pageable);
}
