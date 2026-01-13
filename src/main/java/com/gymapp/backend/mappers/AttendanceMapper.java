package com.gymapp.backend.mappers;

import com.gymapp.backend.dtos.responses.AttendanceResponse;
import com.gymapp.backend.entities.Attendance;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;

@Mapper(componentModel = "spring")
public interface AttendanceMapper {
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", expression = "java(attendance.getMember().getFullName())")
    @Mapping(target = "durationMinutes", expression = "java(calculateDuration(attendance))")
    AttendanceResponse toResponse(Attendance attendance);

    default Long calculateDuration(Attendance attendance) {
        if (attendance.getCheckOutTime() == null) {
            return null;
        }
        return Duration.between(attendance.getCheckInTime(), attendance.getCheckOutTime()).toMinutes();
    }
}
