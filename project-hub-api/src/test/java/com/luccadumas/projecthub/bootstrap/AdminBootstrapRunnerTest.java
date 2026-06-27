package com.luccadumas.projecthub.bootstrap;

import com.luccadumas.projecthub.config.AdminBootstrapProperties;
import com.luccadumas.projecthub.domain.enums.UserRole;
import com.luccadumas.projecthub.entity.AppUser;
import com.luccadumas.projecthub.repository.AppUserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminBootstrapRunnerTest {

    @Mock
    private AppUserRepository appUserRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AdminBootstrapProperties bootstrapProperties;

    @InjectMocks
    private AdminBootstrapRunner adminBootstrapRunner;

    @Test
    @DisplayName("Should create bootstrap admin when database has no users")
    void shouldCreateBootstrapAdminWhenDatabaseIsEmpty() throws Exception {
        when(bootstrapProperties.isConfigured()).thenReturn(true);
        when(bootstrapProperties.getUsername()).thenReturn("ops-admin");
        when(bootstrapProperties.getPassword()).thenReturn("S3cret!");
        when(appUserRepository.count()).thenReturn(0L);
        when(passwordEncoder.encode("S3cret!")).thenReturn("encoded-password");

        adminBootstrapRunner.run(null);

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());
        assertThat(captor.getValue().getUsername()).isEqualTo("ops-admin");
        assertThat(captor.getValue().getPasswordHash()).isEqualTo("encoded-password");
        assertThat(captor.getValue().getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    @DisplayName("Should skip bootstrap when users already exist")
    void shouldSkipBootstrapWhenUsersAlreadyExist() throws Exception {
        when(bootstrapProperties.isConfigured()).thenReturn(true);
        when(appUserRepository.count()).thenReturn(1L);

        adminBootstrapRunner.run(null);

        verify(appUserRepository, never()).save(any());
    }
}
