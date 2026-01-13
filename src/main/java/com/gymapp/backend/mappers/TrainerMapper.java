package com.gymapp.backend.mappers;

import com.gymapp.backend.dtos.requests.CreateTrainerRequest;
import com.gymapp.backend.dtos.requests.UpdateTrainerRequest;
import com.gymapp.backend.dtos.responses.TrainerResponse;
import com.gymapp.backend.entities.Trainer;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TrainerMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "trainingSessions", ignore = true)
    Trainer toEntity(CreateTrainerRequest request);

    TrainerResponse toResponse(Trainer trainer);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "trainingSessions", ignore = true)
    void updateEntity(UpdateTrainerRequest request, @MappingTarget Trainer trainer);
}
