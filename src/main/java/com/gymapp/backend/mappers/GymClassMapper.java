package com.gymapp.backend.mappers;

import com.gymapp.backend.dtos.requests.CreateGymClassRequest;
import com.gymapp.backend.dtos.requests.UpdateGymClassRequest;
import com.gymapp.backend.dtos.responses.GymClassResponse;
import com.gymapp.backend.entities.GymClass;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface GymClassMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentEnrollment", constant = "0")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    GymClass toEntity(CreateGymClassRequest request);

    @Mapping(target = "availableSpots", expression = "java(gymClass.getMaxCapacity() - gymClass.getCurrentEnrollment())")
    GymClassResponse toResponse(GymClass gymClass);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "currentEnrollment", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "enrollments", ignore = true)
    void updateEntity(UpdateGymClassRequest request, @MappingTarget GymClass gymClass);
}
