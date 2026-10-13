package com.mediconnect.backend.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("MediConnect AI - Online Consultation Management System API")
                        .description("Production-grade Spring Boot 3 REST API backend powering MediConnect AI. " +
                                "Provides comprehensive endpoints for user authentication, doctor search & scheduling, " +
                                "conflict-free appointments, telehealth consultations, digital prescriptions, " +
                                "clinical notes, and patient reviews.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("MediConnect Engineering Team")
                                .email("engineering@mediconnect.ai")
                                .url("https://mediconnect.ai"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                                .name(SECURITY_SCHEME_NAME)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("Enter your JWT token (without 'Bearer ' prefix)")));
    }
}

