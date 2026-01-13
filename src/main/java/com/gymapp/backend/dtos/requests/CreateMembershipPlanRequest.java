package com.gymapp.backend.dtos.requests;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateMembershipPlanRequest {
    @NotBlank(message = "Plan name is required")
    @Size(min = 2, max = 50, message = "Plan name must be between 2 and 50 characters")
    private String name;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    @Max(value = 24, message = "Duration cannot exceed 24 months")
    private Integer durationMonths;

    private boolean includesPersonalTraining;

    private boolean includesGroupClasses;

    @Min(value = 0, message = "Max classes per month cannot be negative")
    private Integer maxClassesPerMonth;
}
