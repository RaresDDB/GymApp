package com.gymapp.backend.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI gymAppOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Gym Membership API")
                .description("REST API for managing gym memberships, members, trainers, classes, and attendance")
                .version("1.0.0")
                .contact(new Contact()
                    .name("GymApp Support")
                    .email("support@gymapp.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
        .servers(List.of(
            new Server()
                .url("http://localhost:8080")
                .description("Development Server")));
    }
}