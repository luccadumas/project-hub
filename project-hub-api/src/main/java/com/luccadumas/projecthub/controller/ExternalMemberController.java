package com.luccadumas.projecthub.controller;

import com.luccadumas.projecthub.dto.request.MemberCreateRequest;
import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.service.ExternalMemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/external/members")
@RequiredArgsConstructor
@Tag(name = "External Members", description = "Mock external member API")
public class ExternalMemberController {

    private final ExternalMemberService externalMemberService;

    @GetMapping
    @Operation(summary = "List all members from external service")
    public List<MemberResponse> list() {
        return externalMemberService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get member by id from external service")
    public MemberResponse getById(@PathVariable Long id) {
        return externalMemberService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create member in external service")
    public MemberResponse create(@Valid @RequestBody MemberCreateRequest request) {
        return externalMemberService.create(request);
    }
}
