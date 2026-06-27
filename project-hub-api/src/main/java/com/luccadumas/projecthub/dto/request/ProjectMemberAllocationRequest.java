package com.luccadumas.projecthub.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class ProjectMemberAllocationRequest {

    @NotEmpty(message = "At least one member must be provided")
    @Size(min = 1, max = 10, message = "A project must have between 1 and 10 members")
    private Set<@NotNull Long> memberIds;
}
