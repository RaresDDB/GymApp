package com.gymapp.backend.repositories;

import com.gymapp.backend.entities.MembershipPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MembershipPlanRepository extends JpaRepository<MembershipPlan, UUID> {
    Optional<MembershipPlan> findByName(String name);

    boolean existsByName(String name);

    List<MembershipPlan> findByActiveTrue();

    List<MembershipPlan> findByActiveTrueOrderByPriceAsc();
}
