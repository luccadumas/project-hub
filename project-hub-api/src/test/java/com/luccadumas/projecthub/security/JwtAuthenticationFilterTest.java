package com.luccadumas.projecthub.security;

import com.luccadumas.projecthub.config.JwtProperties;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    private static final String SECRET = "test-secret-key-at-least-32-characters-long";

    @Mock
    private JwtService jwtService;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("Should authenticate request with valid bearer token")
    void shouldAuthenticateValidBearerToken() throws Exception {
        String token = "valid-token";
        request.addHeader("Authorization", "Bearer " + token);

        var claims = Jwts.claims()
                .subject("admin")
                .add("roles", List.of("ROLE_ADMIN"))
                .build();

        when(jwtService.isTokenValid(token)).thenReturn(true);
        when(jwtService.parseToken(token)).thenReturn(claims);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(SecurityContextHolder.getContext().getAuthentication().getName()).isEqualTo("admin");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should skip authentication when bearer token is invalid")
    void shouldSkipInvalidBearerToken() throws Exception {
        String token = "invalid-token";
        request.addHeader("Authorization", "Bearer " + token);
        when(jwtService.isTokenValid(token)).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).parseToken(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should continue filter chain when authorization header is missing")
    void shouldContinueWithoutAuthorizationHeader() throws Exception {
        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(jwtService, never()).isTokenValid(any());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should generate and validate token through JwtService")
    void shouldGenerateAndValidateTokenThroughJwtService() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setAccessTokenExpirationMinutes(15);

        JwtService service = new JwtService(properties);
        var user = org.springframework.security.core.userdetails.User.builder()
                .username("admin")
                .password("secret")
                .authorities("ROLE_ADMIN")
                .build();

        String token = service.generateAccessToken(user);

        assertThat(service.isTokenValid(token)).isTrue();
        assertThat(service.parseToken(token).getSubject()).isEqualTo("admin");
        assertThat(service.getAccessTokenExpirationSeconds()).isEqualTo(900L);
    }

    @Test
    @DisplayName("Should reject malformed token in JwtService")
    void shouldRejectMalformedToken() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setAccessTokenExpirationMinutes(15);

        JwtService service = new JwtService(properties);

        assertThat(service.isTokenValid("not-a-jwt")).isFalse();
    }

    @Test
    @DisplayName("Should reject token signed with different secret")
    void shouldRejectTokenWithDifferentSecret() {
        JwtProperties properties = new JwtProperties();
        properties.setSecret(SECRET);
        properties.setAccessTokenExpirationMinutes(15);
        JwtService service = new JwtService(properties);

        SecretKey otherKey = Keys.hmacShaKeyFor(
                "another-secret-key-at-least-32-chars".getBytes(StandardCharsets.UTF_8));
        String foreignToken = Jwts.builder()
                .subject("admin")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 60_000))
                .signWith(otherKey)
                .compact();

        assertThat(service.isTokenValid(foreignToken)).isFalse();
    }
}
