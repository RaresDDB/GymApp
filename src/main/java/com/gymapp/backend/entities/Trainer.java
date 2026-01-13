package com.gymapp.backend.entities;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "trainers")
@DiscriminatorValue("TRAINER")
@PrimaryKeyJoinColumn(name = "user_id")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Trainer extends User {
    @Column(length = 1000)
    private String bio;

    @Column(length = 100)
    private String specialization;

    @Column(precision = 10, scale = 2)
    private BigDecimal hourlyRate;

    @Builder.Default
    @OneToMany(mappedBy = "trainer", cascade = CascadeType.ALL)
    private List<TrainingSession> trainingSessions = new ArrayList<>();
}