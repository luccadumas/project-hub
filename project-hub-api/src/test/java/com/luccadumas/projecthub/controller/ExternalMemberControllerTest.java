package com.luccadumas.projecthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luccadumas.projecthub.config.JwtProperties;
import com.luccadumas.projecthub.config.SecurityConfig;
import com.luccadumas.projecthub.config.SecurityProperties;
import com.luccadumas.projecthub.dto.request.MemberCreateRequest;
import com.luccadumas.projecthub.dto.response.MemberResponse;
import com.luccadumas.projecthub.exception.GlobalExceptionHandler;
import com.luccadumas.projecthub.security.DatabaseUserDetailsService;
import com.luccadumas.projecthub.security.JwtAuthenticationFilter;
import com.luccadumas.projecthub.security.JwtService;
import com.luccadumas.projecthub.service.ExternalMemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ExternalMemberController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class, JwtService.class, DatabaseUserDetailsService.class})
@EnableConfigurationProperties({SecurityProperties.class, JwtProperties.class})
@TestPropertySource(properties = {
        "project-hub.jwt.secret=test-secret-key-at-least-32-characters-long",
        "project-hub.jwt.access-token-expiration-minutes=15",
        "project-hub.jwt.refresh-token-expiration-days=7"
})
class ExternalMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExternalMemberService externalMemberService;

    @MockBean
    private com.luccadumas.projecthub.repository.AppUserRepository appUserRepository;

    @Test
    @DisplayName("Should require authentication to list external members")
    void shouldRequireAuthenticationToList() throws Exception {
        mockMvc.perform(get("/external/members"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should list external members for authenticated user")
    void shouldListExternalMembers() throws Exception {
        when(externalMemberService.findAll()).thenReturn(List.of(
                MemberResponse.builder().id(1L).name("Ana Silva").role("manager").build()
        ));

        mockMvc.perform(get("/external/members"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Ana Silva"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should get external member by id")
    void shouldGetExternalMemberById() throws Exception {
        when(externalMemberService.findById(1L)).thenReturn(
                MemberResponse.builder().id(1L).name("Ana Silva").role("manager").build()
        );

        mockMvc.perform(get("/external/members/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("manager"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("Should block non-admin from creating external members")
    void shouldBlockNonAdminFromCreating() throws Exception {
        MemberCreateRequest request = new MemberCreateRequest();
        request.setName("Novo");
        request.setRole("MANAGER");

        mockMvc.perform(post("/external/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should allow admin to create external members")
    void shouldAllowAdminToCreate() throws Exception {
        MemberCreateRequest request = new MemberCreateRequest();
        request.setName("Novo");
        request.setRole("MANAGER");

        when(externalMemberService.create(any())).thenReturn(
                MemberResponse.builder().id(10L).name("Novo").role("manager").build()
        );

        mockMvc.perform(post("/external/members")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10));
    }
}
