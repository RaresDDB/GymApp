package com.gymapp.backend.mappers;

import com.gymapp.backend.dtos.responses.SubscriptionResponse;
import com.gymapp.backend.entities.Subscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SubscriptionMapper {
    @Mapping(target = "planId", source = "membershipPlan.id")
    @Mapping(target = "planName", source = "membershipPlan.name")
    @Mapping(target = "memberId", source = "member.id")
    @Mapping(target = "memberName", expression = "java(subscription.getMember().getFullName())")
    SubscriptionResponse toResponse(Subscription subscription);
}