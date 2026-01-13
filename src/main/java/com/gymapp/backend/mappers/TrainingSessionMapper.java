package com.gymapp.backend.mappers;

import com.gymapp.backend.dtos.responses.TrainingSessionResponse;
import com.gymapp.backend.entities.TrainingSession;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TrainingSessionMapper {
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "trainerId", source = "trainer.id")
    @Mapping(target = "memberName", expression = "java(session.getMember().getFullName())")
    @Mapping(target = "trainerName", expression = "java(session.getTrainer().getFullName())")
    TrainingSessionResponse toResponse(TrainingSession session);
}