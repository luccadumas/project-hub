package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.dto.request.LoginRequest;
import com.luccadumas.projecthub.dto.response.TokenResponse;
import com.luccadumas.projecthub.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class AuthServiceIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("project_hub")
            .withUsername("project_hub")
            .withPassword("project_hub");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("project-hub.jwt.secret", () -> "test-secret-key-at-least-32-characters-long");
    }

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Test
    @DisplayName("Should authenticate seeded admin user and issue tokens")
    void shouldAuthenticateSeededAdminUser() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        TokenResponse response = authService.login(request);

        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRoles()).containsExactly("ADMIN");
        assertThat(response.getAccessToken()).isNotBlank();
        assertThat(response.getRefreshToken()).isNotBlank();
        assertThat(jwtService.isTokenValid(response.getAccessToken())).isTrue();
    }

    @Test
    @DisplayName("Should authenticate seeded user account")
    void shouldAuthenticateSeededUserAccount() {
        LoginRequest request = new LoginRequest();
        request.setUsername("user");
        request.setPassword("user123");

        TokenResponse response = authService.login(request);

        assertThat(response.getUsername()).isEqualTo("user");
        assertThat(response.getRoles()).containsExactly("USER");
    }
}
