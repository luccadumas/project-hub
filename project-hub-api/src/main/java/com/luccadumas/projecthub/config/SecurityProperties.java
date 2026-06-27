package com.luccadumas.projecthub.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "project-hub.security")
public class SecurityProperties {

    @NotBlank
    private String corsAllowedOrigins = "http://localhost:5190,http://localhost:5173";
}
