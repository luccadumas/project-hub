package com.luccadumas.projecthub.bootstrap;

import com.luccadumas.projecthub.config.AdminBootstrapProperties;
import com.luccadumas.projecthub.domain.enums.UserRole;
import com.luccadumas.projecthub.entity.AppUser;
import com.luccadumas.projecthub.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "project-hub.bootstrap.admin", name = "username")
public class AdminBootstrapRunner implements ApplicationRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminBootstrapProperties bootstrapProperties;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!bootstrapProperties.isConfigured()) {
            return;
        }

        if (appUserRepository.count() > 0) {
            log.debug("Skipping admin bootstrap because users already exist");
            return;
        }

        AppUser admin = AppUser.builder()
                .username(bootstrapProperties.getUsername())
                .passwordHash(passwordEncoder.encode(bootstrapProperties.getPassword()))
                .role(UserRole.ADMIN)
                .enabled(true)
                .build();

        appUserRepository.save(admin);
        log.info("Bootstrap admin user created: {}", admin.getUsername());
    }
}
