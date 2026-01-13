package com.gymapp.backend.repositories;

import com.gymapp.backend.entities.Subscription;
import com.gymapp.backend.enums.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    Optional<Subscription> findByMemberId(UUID memberId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.endDate < :date AND s.status = :status")
    List<Subscription> findExpiredSubscriptions(@Param("date") LocalDate date, @Param("status") SubscriptionStatus status);

    @Query("SELECT s FROM Subscription s WHERE s.endDate BETWEEN :startDate AND :endDate AND s.status = 'ACTIVE'")
    List<Subscription> findSubscriptionsExpiringBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByMemberIdAndStatus(UUID memberId, SubscriptionStatus status);
}
