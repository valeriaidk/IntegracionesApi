package com.extech.IntegracionesApis.Config.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Permitir acceso a Swagger y OpenAPI
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                // Permitir acceso a APIs y actuador
                .requestMatchers("/api/**").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                // Home y error
                .requestMatchers("/", "/error").permitAll()
                // Todo lo demás requiere autenticación
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
