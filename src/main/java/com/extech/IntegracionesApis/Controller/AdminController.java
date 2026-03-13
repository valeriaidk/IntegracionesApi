package com.extech.IntegracionesApis.Controller;

import com.extech.IntegracionesApis.Service.Auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:58918", "http://127.0.0.1:56114"}, allowCredentials = "false")
public class AdminController {
    
    private final AuthService authService;
    
    @PostMapping("/init-user")
    public ResponseEntity<Map<String, Object>> initializeUser() {
        try {
            // Crear usuario extech si no existe
            var user = authService.createUser(
                "extech", 
                "extech123", 
                "Usuario EXTECH", 
                "usuario@extech.com", 
                null // Sin plan predefinido
            );
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario creado exitosamente",
                "user", Map.of(
                    "username", user.getUsername(),
                    "fullName", user.getFullName(),
                    "email", user.getEmail(),
                    "planType", user.getPlanType(),
                    "createdAt", user.getCreatedAt()
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));
        }
    }
}
