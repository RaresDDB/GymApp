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
public class CreateTrainerRequest {
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;

    @Size(max = 100, message = "Specialization cannot exceed 100 characters")
    private String specialization;

    @Size(max = 1000, message = "Bio cannot exceed 1000 characters")
    private String bio;

    @DecimalMin(value = "0.01", message = "Hourly rate must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Invalid hourly rate format")
    private BigDecimal hourlyRate;
}
