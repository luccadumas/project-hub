package com.luccadumas.projecthub.mapper;

import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.entity.Member;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MemberMapper {

    @Mapping(target = "role", expression = "java(member.getRole().name().toLowerCase())")
    MemberResponse toResponse(Member member);
}
