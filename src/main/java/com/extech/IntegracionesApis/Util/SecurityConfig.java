package com.extech.IntegracionesApis.Config.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyAuthFilter apiKeyAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .addFilterBefore(apiKeyAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()
                .requestMatchers("/api/auth/login").permitAll()
                .requestMatchers("/api/apis-externas/**").permitAll()
                .requestMatchers("/api/**").authenticated()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/", "/error").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
}
