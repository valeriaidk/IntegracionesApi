package com.extech.IntegracionesApis.Service.Auth;

import com.extech.IntegracionesApis.Domain.Dto.Auth.LoginRequest;
import com.extech.IntegracionesApis.Domain.Dto.Auth.LoginResponse;
import com.extech.IntegracionesApis.Domain.Entities.User;
import com.extech.IntegracionesApis.Repository.UserRepository;
import com.extech.IntegracionesApis.Service.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    public LoginResponse autenticar(LoginRequest loginRequest) {
        log.info("Intentando autenticar usuario: {}", loginRequest.getUsuario());
        
        // Buscar usuario en la base de datos
        Optional<User> userOpt = userRepository.findActiveByUsername(loginRequest.getUsuario());
        
        if (userOpt.isEmpty()) {
            log.warn("Usuario no encontrado o inactivo: {}", loginRequest.getUsuario());
            throw new RuntimeException("Credenciales inválidas");
        }
        
        User user = userOpt.get();
        
        // Validar contraseña (en producción usarías BCrypt)
        if (!user.getPassword().equals(loginRequest.getContrasena())) {
            log.warn("Contraseña incorrecta para usuario: {}", loginRequest.getUsuario());
            throw new RuntimeException("Credenciales inválidas");
        }
        
        // Actualizar último login
        user.updateLastLogin();
        userRepository.save(user);
        
        // Generar token JWT real y guardarlo en la base de datos
        String token = jwtService.generateToken(user.getUsername(), user.getPassword(), user.getPlanType());
        
        // Guardar token en el usuario (podrías tener un campo específico para tokens)
        user.setLastToken(token);
        userRepository.save(user);
        
        log.info("Usuario autenticado exitosamente: {}", user.getUsername());
        log.info("Token guardado en base de datos para usuario: {}", user.getUsername());
        
        // Crear respuesta
        LoginResponse.UsuarioData usuarioData = new LoginResponse.UsuarioData(
            user.getUsername(),
            user.getUsername(),
            user.getFullName(),
            user.getEmail(),
            user.getCreatedAt().format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy")),
            user.getIsVerified(),
            user.getPlanType()
        );
        
        return new LoginResponse(token, usuarioData);
    }
    
    public User createUser(String username, String password, String fullName, String email, String planType) {
        // Verificar si el usuario ya existe
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("El nombre de usuario ya existe");
        }
        
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Crear nuevo usuario
        User newUser = User.builder()
                .username(username)
                .password(password) // En producción usarías BCrypt
                .fullName(fullName)
                .email(email)
                .planType(planType)
                .isActive(true)
                .isVerified(true)
                .build();
        
        User savedUser = userRepository.save(newUser);
        log.info("Usuario creado en base de datos: {}", savedUser.getUsername());
        
        return savedUser;
    }
    
    public String generateApiToken(String username) {
        Optional<User> userOpt = userRepository.findActiveByUsername(username);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }
        
        User user = userOpt.get();
        String apiToken = jwtService.generateApiToken(user.getUsername(), user.getPlanType());
        
        // Guardar el API token en la base de datos
        user.setApiToken(apiToken);
        userRepository.save(user);
        
        log.info("API Token generado y guardado en base de datos para usuario: {}", username);
        
        return apiToken;
    }
    
    public boolean validateToken(String token) {
        try {
            String username = jwtService.extractUsername(token);
            Optional<User> userOpt = userRepository.findActiveByUsername(username);
            
            if (userOpt.isEmpty()) {
                return false;
            }
            
            User user = userOpt.get();
            
            // Verificar que el token guardado coincida
            if (!token.equals(user.getLastToken())) {
                log.warn("Token no coincide con el guardado en base de datos para usuario: {}", username);
                return false;
            }
            
            return jwtService.validateToken(token, username);
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return false;
        }
    }
}
