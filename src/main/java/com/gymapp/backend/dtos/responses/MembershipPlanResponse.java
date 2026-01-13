package com.gymapp.backend.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipPlanResponse {
    private UUID id;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer durationMonths;
    private boolean includesPersonalTraining;
    private boolean includesGroupClasses;
    private Integer maxClassesPerMonth;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
