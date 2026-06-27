package com.luccadumas.projecthub;

import com.luccadumas.projecthub.config.AdminBootstrapProperties;
import com.luccadumas.projecthub.config.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SecurityProperties.class, AdminBootstrapProperties.class})
public class ProjectHubApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectHubApplication.class, args);
    }
}
