package com.luccadumas.projecthub.service;

import com.luccadumas.projecthub.config.JwtProperties;
import com.luccadumas.projecthub.dto.request.LoginRequest;
import com.luccadumas.projecthub.dto.request.RefreshTokenRequest;
import com.luccadumas.projecthub.dto.response.TokenResponse;
import com.luccadumas.projecthub.entity.RefreshToken;
import com.luccadumas.projecthub.repository.RefreshTokenRepository;
import com.luccadumas.projecthub.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return buildTokenResponse(userDetails, createRefreshToken(userDetails.getUsername()));
    }

    @Transactional
    public TokenResponse refresh(RefreshTokenRequest request) {
        RefreshToken storedToken = refreshTokenRepository.findById(request.getRefreshToken())
                .filter(token -> !token.isRevoked())
                .filter(token -> token.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(() -> new BadCredentialsException("Invalid or expired refresh token"));

        UserDetails userDetails = userDetailsService.loadUserByUsername(storedToken.getUsername());
        refreshTokenRepository.revokeByToken(storedToken.getToken());

        return buildTokenResponse(userDetails, createRefreshToken(userDetails.getUsername()));
    }

    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findById(request.getRefreshToken())
                .ifPresent(token -> refreshTokenRepository.revokeByToken(token.getToken()));
    }

    private TokenResponse buildTokenResponse(UserDetails userDetails, String refreshToken) {
        String accessToken = jwtService.generateAccessToken(userDetails);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .map(authority -> authority.replace("ROLE_", ""))
                .toList();

        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getAccessTokenExpirationSeconds())
                .username(userDetails.getUsername())
                .roles(roles)
                .build();
    }

    private String createRefreshToken(String username) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .username(username)
                .expiresAt(Instant.now().plusSeconds(jwtProperties.getRefreshTokenExpirationDays() * 24 * 3600))
                .revoked(false)
                .build();

        return refreshTokenRepository.save(refreshToken).getToken();
    }
}
