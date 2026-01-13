package com.gymapp.backend.mappers;

import com.gymapp.backend.dtos.requests.CreateMembershipPlanRequest;
import com.gymapp.backend.dtos.requests.UpdateMembershipPlanRequest;
import com.gymapp.backend.dtos.responses.MembershipPlanResponse;
import com.gymapp.backend.entities.MembershipPlan;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface MembershipPlanMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    MembershipPlan toEntity(CreateMembershipPlanRequest request);

    MembershipPlanResponse toResponse(MembershipPlan plan);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscriptions", ignore = true)
    void updateEntity(UpdateMembershipPlanRequest request, @MappingTarget MembershipPlan plan);
}
