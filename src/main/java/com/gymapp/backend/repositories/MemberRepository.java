package com.gymapp.backend.repositories;

import com.gymapp.backend.entities.Member;
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
public interface MemberRepository extends JpaRepository<Member, UUID> {
    Optional<Member> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<Member> findByActiveTrue(Pageable pageable);

    @Query("SELECT m FROM Member m WHERE m.active = true AND " +
            "(LOWER(m.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
            "LOWER(m.email) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Member> searchMembers(@Param("search") String search, Pageable pageable);

    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.subscription WHERE m.id = :id")
    Optional<Member> findByIdWithSubscription(@Param("id") UUID id);

    List<Member> findByActiveTrueAndSubscriptionIsNull();
}
