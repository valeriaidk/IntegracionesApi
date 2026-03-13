package com.extech.IntegracionesApis.Config;

import com.extech.IntegracionesApis.Domain.Entities.User;
import com.extech.IntegracionesApis.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {
    
    private final UserRepository userRepository;
    
    @Bean
    public CommandLineRunner initDatabase() {
        return args -> {
            log.info("Inicializando datos de la base de datos...");
            
            // Verificar si el usuario extech ya existe
            if (!userRepository.existsByUsername("extech")) {
                // Crear usuario por defecto
                User defaultUser = User.builder()
                        .username("extech")
                        .password("extech123") // En producción usarías BCrypt
                        .fullName("Usuario EXTECH")
                        .email("usuario@extech.com")
                        .planType(null) // Sin plan predefinido
                        .isActive(true)
                        .isVerified(true)
                        .build();
                
                userRepository.save(defaultUser);
                log.info("Usuario por defecto creado: extech");
            } else {
                log.info("Usuario extech ya existe en la base de datos");
            }
            
            log.info("Inicialización de datos completada");
        };
    }
}
