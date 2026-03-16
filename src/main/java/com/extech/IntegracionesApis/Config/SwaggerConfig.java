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
                                .description("""
                                        API para el envío y gestión de mensajes SMS mediante Infobip

                                        Características principales:
                                        - Envío de SMS individual y masivo
                                        - Validación de números y mensajes
                                        - Simulación de persistencia
                                        - Estadísticas y reportes detallados
                                        - Manejo de errores específico con logging
                                        - Soporte para múltiples proveedores

                                        Configuración requerida:
                                        - API Key de Infobip en application.properties
                                        - Conexión a internet para envío de SMS

                                        Formato de números:
                                        - Perú: +51XXXXXXXXX (ej: +51987654321)
                                        - Internacional: +CódigoPaísNúmero

                                        Límites:
                                        - Mensaje individual: máximo 160 caracteres
                                        - Envío masivo: máximo 100 mensajes por solicitud
                                        - Timeouts: 10s conexión, 30s lectura"""),
                        new Tag()
                                .name("Autenticación")
                                .description("Endpoints para autenticación y gestión de tokens")
                ));
    }
}
