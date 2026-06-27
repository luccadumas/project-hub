package com.luccadumas.projecthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luccadumas.projecthub.config.JwtProperties;
import com.luccadumas.projecthub.config.SecurityConfig;
import com.luccadumas.projecthub.config.SecurityProperties;
import com.luccadumas.projecthub.domain.enums.ProjectStatus;
import com.luccadumas.projecthub.dto.request.ProjectCreateRequest;
import com.luccadumas.projecthub.dto.response.ProjectResponse;
import com.luccadumas.projecthub.exception.GlobalExceptionHandler;
import com.luccadumas.projecthub.security.DatabaseUserDetailsService;
import com.luccadumas.projecthub.security.JwtAuthenticationFilter;
import com.luccadumas.projecthub.security.JwtService;
import com.luccadumas.projecthub.service.ProjectService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProjectController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class, JwtService.class, DatabaseUserDetailsService.class})
@EnableConfigurationProperties({SecurityProperties.class, JwtProperties.class})
@TestPropertySource(properties = {
        "project-hub.jwt.secret=test-secret-key-at-least-32-characters-long",
        "project-hub.jwt.access-token-expiration-minutes=15",
        "project-hub.jwt.refresh-token-expiration-days=7"
})
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private com.luccadumas.projecthub.repository.AppUserRepository appUserRepository;

    @Test
    @DisplayName("Should require authentication for project listing")
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should allow authenticated user to list projects")
    void shouldAllowAuthenticatedUserToListProjects() throws Exception {
        when(projectService.findAll(any(), any(), any(), any(), any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(
                        ProjectResponse.builder().id(1L).name("Portal").status(ProjectStatus.EM_ANALISE).build()
                )));

        mockMvc.perform(get("/api/projects?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Portal"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should block non-admin from creating projects")
    void shouldBlockNonAdminFromCreatingProjects() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Novo");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setExpectedEndDate(LocalDate.of(2025, 4, 1));
        request.setTotalBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Set.of(2L));

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should allow admin to create projects")
    void shouldAllowAdminToCreateProjects() throws Exception {
        ProjectCreateRequest request = new ProjectCreateRequest();
        request.setName("Novo");
        request.setStartDate(LocalDate.of(2025, 1, 1));
        request.setExpectedEndDate(LocalDate.of(2025, 4, 1));
        request.setTotalBudget(new BigDecimal("50000"));
        request.setManagerId(1L);
        request.setMemberIds(Set.of(2L));

        when(projectService.create(any())).thenReturn(
                ProjectResponse.builder().id(10L).name("Novo").status(ProjectStatus.EM_ANALISE).build()
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }
}
