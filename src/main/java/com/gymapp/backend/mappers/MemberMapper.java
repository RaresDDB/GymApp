package com.gymapp.backend.mappers;

import com.gymapp.backend.dtos.requests.CreateMemberRequest;
import com.gymapp.backend.dtos.requests.UpdateMemberRequest;
import com.gymapp.backend.dtos.responses.MemberResponse;
import com.gymapp.backend.entities.Member;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {SubscriptionMapper.class})
public interface MemberMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "attendanceRecords", ignore = true)
    @Mapping(target = "trainingSessions", ignore = true)
    @Mapping(target = "classEnrollments", ignore = true)
    Member toEntity(CreateMemberRequest request);

    @Mapping(target = "subscription", source = "subscription")
    MemberResponse toResponse(Member member);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subscription", ignore = true)
    @Mapping(target = "attendanceRecords", ignore = true)
    @Mapping(target = "trainingSessions", ignore = true)
    @Mapping(target = "classEnrollments", ignore = true)
    void updateEntity(UpdateMemberRequest request, @MappingTarget Member member);
}