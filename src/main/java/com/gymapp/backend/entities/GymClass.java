package com.gymapp.backend.entities;

import com.gymapp.backend.enums.ClassType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "gym_classes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GymClass {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false, length = 100)
    private String instructor;

    @Column(nullable = false)
    private Integer maxCapacity;

    @Builder.Default
    @Column(nullable = false)
    private Integer currentEnrollment = 0;

    @Column(nullable = false)
    private LocalDateTime scheduledAt;

    @Builder.Default
    @Column(nullable = false)
    private Integer durationMinutes = 60;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ClassType classType;

    @Builder.Default
    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder.Default
    @OneToMany(mappedBy = "gymClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ClassEnrollment> enrollments = new ArrayList<>();
}