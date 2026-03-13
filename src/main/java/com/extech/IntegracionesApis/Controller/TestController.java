package com.extech.IntegracionesApis.Controller;

import com.extech.IntegracionesApis.Service.Auth.AuthService;
import com.extech.IntegracionesApis.Service.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:58918", "http://127.0.0.1:56114"}, allowCredentials = "false")
public class TestController {
    
    private final AuthService authService;
    private final JwtService jwtService;
    
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status() {
        return ResponseEntity.ok(Map.of(
            "status", "Backend funcionando",
            "database", "Conectada",
            "jwt", "Configurado"
        ));
    }
    
    @PostMapping("/create-user")
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            String fullName = request.get("fullName");
            String email = request.get("email");
            String planType = request.get("planType");
            
            var user = authService.createUser(username, password, fullName, email, planType);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Usuario creado en base de datos",
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
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/generate-token")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            String token = jwtService.generateToken(username, password, "PREMIUM");
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "message", "Token JWT generado y guardado en base de datos"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
