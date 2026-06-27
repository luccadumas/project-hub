package com.luccadumas.projecthub.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.luccadumas.projecthub.config.JwtProperties;
import com.luccadumas.projecthub.config.SecurityConfig;
import com.luccadumas.projecthub.config.SecurityProperties;
import com.luccadumas.projecthub.dto.request.LoginRequest;
import com.luccadumas.projecthub.dto.request.RefreshTokenRequest;
import com.luccadumas.projecthub.dto.response.TokenResponse;
import com.luccadumas.projecthub.exception.GlobalExceptionHandler;
import com.luccadumas.projecthub.security.DatabaseUserDetailsService;
import com.luccadumas.projecthub.security.JwtAuthenticationFilter;
import com.luccadumas.projecthub.security.JwtService;
import com.luccadumas.projecthub.service.AuthService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AuthController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class, JwtAuthenticationFilter.class, JwtService.class, DatabaseUserDetailsService.class})
@EnableConfigurationProperties({SecurityProperties.class, JwtProperties.class})
@TestPropertySource(properties = {
        "project-hub.jwt.secret=test-secret-key-at-least-32-characters-long",
        "project-hub.jwt.access-token-expiration-minutes=15",
        "project-hub.jwt.refresh-token-expiration-days=7"
})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private com.luccadumas.projecthub.repository.AppUserRepository appUserRepository;

    @Test
    @DisplayName("Should require authentication for profile endpoint")
    void shouldRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Should return authenticated profile and roles")
    void shouldReturnAuthenticatedProfile() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("ADMIN"));
    }

    @Test
    @DisplayName("Should login and return tokens")
    void shouldLoginAndReturnTokens() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(
                TokenResponse.builder()
                        .accessToken("access-token")
                        .refreshToken("refresh-token")
                        .tokenType("Bearer")
                        .expiresIn(900)
                        .username("admin")
                        .roles(List.of("ADMIN"))
                        .build()
        );

        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.username").value("admin"));
    }

    @Test
    @DisplayName("Should refresh tokens")
    void shouldRefreshTokens() throws Exception {
        when(authService.refresh(any(RefreshTokenRequest.class))).thenReturn(
                TokenResponse.builder()
                        .accessToken("new-access-token")
                        .refreshToken("new-refresh-token")
                        .tokenType("Bearer")
                        .expiresIn(900)
                        .username("admin")
                        .roles(List.of("ADMIN"))
                        .build()
        );

        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        mockMvc.perform(post("/api/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("new-access-token"));
    }

    @Test
    @DisplayName("Should logout and revoke refresh token")
    void shouldLogoutAndRevokeRefreshToken() throws Exception {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        mockMvc.perform(post("/api/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(authService).logout(any(RefreshTokenRequest.class));
    }
}
