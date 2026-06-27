package com.luccadumas.projecthub.dto.request;

import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProjectStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private ProjectStatus status;
}
