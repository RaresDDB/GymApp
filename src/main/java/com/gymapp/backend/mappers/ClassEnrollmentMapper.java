package com.gymapp.backend.mappers;

import com.gymapp.backend.dtos.responses.ClassEnrollmentResponse;
import com.gymapp.backend.entities.ClassEnrollment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClassEnrollmentMapper {
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", expression = "java(enrollment.getMember().getFullName())")
    @Mapping(target = "gymClassId", source = "gymClass.id")
    @Mapping(target = "gymClassName", source = "gymClass.name")
    ClassEnrollmentResponse toResponse(ClassEnrollment enrollment);
}