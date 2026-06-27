package com.luccadumas.projecthub.dto.request;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
public class ProjectUpdateRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 200, message = "Name must have at most 200 characters")
    private String name;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "Expected end date is required")
    private LocalDate expectedEndDate;

    private LocalDate actualEndDate;

    @NotNull(message = "Total budget is required")
    @DecimalMin(value = "0.01", message = "Total budget must be greater than zero")
    private BigDecimal totalBudget;

    @Size(max = 5000, message = "Description must have at most 5000 characters")
    private String description;

    @NotNull(message = "Manager id is required")
    private Long managerId;

    @NotEmpty(message = "At least one member must be allocated")
    @Size(max = 10, message = "A project can have at most 10 members")
    private Set<@NotNull Long> memberIds;
}
