package com.luccadumas.projecthub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "project-hub.jwt")
public class JwtProperties {

    private String secret;
    private long accessTokenExpirationMinutes = 15;
    private long refreshTokenExpirationDays = 7;
}
