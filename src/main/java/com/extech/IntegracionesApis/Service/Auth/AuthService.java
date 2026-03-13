package com.extech.IntegracionesApis.Service.Auth;

import com.extech.IntegracionesApis.Domain.Dto.Auth.LoginRequest;
import com.extech.IntegracionesApis.Domain.Dto.Auth.LoginResponse;
import com.extech.IntegracionesApis.Domain.Model.Usuario;
import com.extech.IntegracionesApis.Repository.UsuarioRepository;
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
    
    private final UsuarioRepository usuarioRepository;
    
    public LoginResponse autenticar(LoginRequest loginRequest) {
        log.info("Intentando autenticar usuario: {}", loginRequest.getUsuario());
        
        // Buscar usuario en la base de datos
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndActivoTrue(loginRequest.getUsuario());
        
        if (usuarioOpt.isEmpty()) {
            log.warn("Usuario no encontrado o inactivo: {}", loginRequest.getUsuario());
            throw new RuntimeException("Credenciales inválidas");
        }
        
        Usuario usuario = usuarioOpt.get();
        
        // Validar contraseña (en producción usarías BCrypt)
        if (!usuario.getPasswordHash().equals(loginRequest.getContrasena())) {
            log.warn("Contraseña incorrecta para usuario: {}", loginRequest.getUsuario());
            throw new RuntimeException("Credenciales inválidas");
        }
        
        // Actualizar último login
        usuario.setFechaModificacion(LocalDateTime.now());
        usuarioRepository.save(usuario);
        
        // Generar respuesta temporal (sin JWT por ahora)
        LoginResponse.UsuarioData usuarioData = new LoginResponse.UsuarioData(
            usuario.getNombre() + " " + usuario.getApellido(),
            usuario.getEmail(),
            usuario.getNombre() + " " + usuario.getApellido(),
            usuario.getEmail(),
            usuario.getFechaRegistro().format(DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy")),
            true, // Asumimos verificado
            "Básico" // Plan por defecto
        );
        
        return new LoginResponse("temp_token", usuarioData);
    }
    
    public Usuario createUser(String username, String password, String fullName, String email, String planType) {
        // Verificar si el email ya existe
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }
        
        // Crear nuevo usuario
        Usuario nuevoUsuario = new Usuario();
        String[] nombres = fullName.split(" ", 2);
        nuevoUsuario.setNombre(nombres.length > 0 ? nombres[0] : fullName);
        nuevoUsuario.setApellido(nombres.length > 1 ? nombres[1] : "");
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPasswordHash(password); // En producción usarías BCrypt
        nuevoUsuario.setActivo(true);
        
        Usuario savedUsuario = usuarioRepository.save(nuevoUsuario);
        log.info("Usuario creado en base de datos: {}", savedUsuario.getEmail());
        
        return savedUsuario;
    }
}
