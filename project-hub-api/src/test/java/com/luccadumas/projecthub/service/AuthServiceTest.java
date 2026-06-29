package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.config.JwtProperties;
import com.luccadumas.projecthub.dto.request.LoginRequest;
import com.luccadumas.projecthub.dto.request.RefreshTokenRequest;
import com.luccadumas.projecthub.dto.response.TokenResponse;
import com.luccadumas.projecthub.entity.RefreshToken;
import com.luccadumas.projecthub.repository.RefreshTokenRepository;
import com.luccadumas.projecthub.security.JwtService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private JwtService jwtService;

    @Mock
    private JwtProperties jwtProperties;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    @DisplayName("Should login and return token response")
    void shouldLoginAndReturnTokens() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        var userDetails = User.builder()
                .username("admin")
                .password("encoded")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();

        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("access-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(jwtProperties.getRefreshTokenExpirationDays()).thenReturn(7L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TokenResponse response = authService.login(request);

        assertThat(response.getAccessToken()).isEqualTo("access-token");
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRoles()).containsExactly("ADMIN");
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    @DisplayName("Should refresh tokens when refresh token is valid")
    void shouldRefreshTokens() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        RefreshToken storedToken = RefreshToken.builder()
                .token("refresh-token")
                .username("admin")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        var userDetails = User.builder()
                .username("admin")
                .password("encoded")
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();

        when(refreshTokenRepository.findById("refresh-token")).thenReturn(Optional.of(storedToken));
        when(userDetailsService.loadUserByUsername("admin")).thenReturn(userDetails);
        when(jwtService.generateAccessToken(userDetails)).thenReturn("new-access-token");
        when(jwtService.getAccessTokenExpirationSeconds()).thenReturn(900L);
        when(jwtProperties.getRefreshTokenExpirationDays()).thenReturn(7L);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TokenResponse response = authService.refresh(request);

        assertThat(response.getAccessToken()).isEqualTo("new-access-token");
        verify(refreshTokenRepository).revokeByToken("refresh-token");
    }

    @Test
    @DisplayName("Should reject refresh with invalid token")
    void shouldRejectInvalidRefreshToken() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid");
        when(refreshTokenRepository.findById("invalid")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(request))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    @DisplayName("Should revoke refresh token on logout")
    void shouldRevokeRefreshTokenOnLogout() {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("refresh-token");

        RefreshToken storedToken = RefreshToken.builder()
                .token("refresh-token")
                .username("admin")
                .expiresAt(Instant.now().plusSeconds(3600))
                .revoked(false)
                .build();

        when(refreshTokenRepository.findById("refresh-token")).thenReturn(Optional.of(storedToken));

        authService.logout(request);

        verify(refreshTokenRepository).revokeByToken("refresh-token");
    }
}
