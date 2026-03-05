package com.extech.IntegracionesApis.Config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Integración de APIs")
                        .description("Documentación de endpoints")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
                .components(new Components()
                        .addSecuritySchemes("Bearer Authentication",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Autenticación JWT (Bearer)")))
                .tags(List.of(
                        new Tag()
                                .name("Reniec")
                                .description("Endpoints para consulta de datos con RENIEC"),
                        new Tag()
                                .name("Sunat")
                                .description("Endpoints para consulta de datos con SUNAT"),
                        new Tag()
                                .name("Correo")
                                .description("Endpoints para envío de correos electrónicos"),
                        new Tag()
                                .name("SMS")
                                .description("Endpoints para envío de mensajes SMS"),
                        new Tag()
                                .name("Autenticación")
                                .description("Endpoints para autenticación y gestión de tokens")
                ));
    }
}
