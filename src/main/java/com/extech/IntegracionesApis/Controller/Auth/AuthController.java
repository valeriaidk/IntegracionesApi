package com.extech.IntegracionesApis.Controller.Auth;

import com.extech.IntegracionesApis.Domain.Dto.Auth.LoginRequest;
import com.extech.IntegracionesApis.Domain.Dto.Auth.LoginResponse;
import com.extech.IntegracionesApis.Service.Auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:58918", "http://127.0.0.1:56114"}, allowCredentials = "false")
public class AuthController {
    
    private final AuthService authService;
    
    @Operation(
        summary = "Autenticar usuario",
        description = "Autentica un usuario y retorna un token JWT"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Autenticación exitosa"),
        @ApiResponse(responseCode = "401", description = "Credenciales inválidas")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Intento de login para usuario: {}", loginRequest.getUsuario());
            
            LoginResponse response = authService.autenticar(loginRequest);
            
            log.info("Login exitoso para usuario: {}", loginRequest.getUsuario());
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            log.error("Error en login: {}", e.getMessage());
            return ResponseEntity.status(401).body(Map.of(
                "error", e.getMessage(),
                "status", 401
            ));
        } catch (Exception e) {
            log.error("Error inesperado en login: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error interno del servidor",
                "details", e.getMessage()
            ));
        }
    }
    
    @Operation(
        summary = "Verificar token",
        description = "Verifica si un token JWT es válido"
    )
    @GetMapping("/verify-token")
    public ResponseEntity<?> verifyToken(@RequestParam String token) {
        try {
            // Lógica para verificar token
            return ResponseEntity.ok(Map.of(
                "valid", true,
                "message", "Token válido"
            ));
        } catch (Exception e) {
            return ResponseEntity.status(401).body(Map.of(
                "valid", false,
                "error", "Token inválido"
            ));
        }
    }
}
