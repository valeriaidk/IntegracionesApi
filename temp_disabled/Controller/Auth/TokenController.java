package com.extech.IntegracionesApis.Controller.Auth;

import com.extech.IntegracionesApis.Service.Auth.AuthService;
import com.extech.IntegracionesApis.Service.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://127.0.0.1:58918"})
public class TokenController {
    
    private final AuthService authService;
    private final JwtService jwtService;
    
    @PostMapping("/generate-token")
    public ResponseEntity<Map<String, Object>> generateToken(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");
            
            if (username == null || password == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Se requieren username y password"
                ));
            }
            
            // Generar token JWT real
            String token = jwtService.generateToken(username, password, "PREMIUM");
            
            // Obtener información del token
            Map<String, Object> tokenInfo = jwtService.getTokenInfo(token);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("token", token);
            response.put("tokenInfo", tokenInfo);
            response.put("usage", "Usa este token en el header Authorization: Bearer <token>");
            response.put("postman", Map.of(
                "type", "Bearer Token",
                "token", token,
                "prefix", "Bearer"
            ));
            
            log.info("Token generado para usuario: {}", username);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generando token: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error generando token: " + e.getMessage()
            ));
        }
    }
    
    @PostMapping("/api-token")
    public ResponseEntity<Map<String, Object>> generateApiToken(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            
            if (username == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Se requiere username"
                ));
            }
            
            // Generar token API
            String apiToken = authService.generateApiToken(username);
            
            // Obtener información del token
            Map<String, Object> tokenInfo = jwtService.getTokenInfo(apiToken);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("apiToken", apiToken);
            response.put("tokenInfo", tokenInfo);
            response.put("usage", "Usa este token para acceder a las APIs");
            response.put("postman", Map.of(
                "type", "API Token",
                "token", apiToken,
                "header", "X-API-Key"
            ));
            
            log.info("API Token generado para usuario: {}", username);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error generando API token: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error generando API token: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/validate-token")
    public ResponseEntity<Map<String, Object>> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Header Authorization debe ser 'Bearer <token>'"
                ));
            }
            
            String token = authHeader.substring(7);
            Map<String, Object> tokenInfo = jwtService.getTokenInfo(token);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "tokenInfo", tokenInfo
            ));
            
        } catch (Exception e) {
            log.error("Error validando token: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error validando token: " + e.getMessage()
            ));
        }
    }
    
    @GetMapping("/token-info")
    public ResponseEntity<Map<String, Object>> getTokenInfo(@RequestParam String token) {
        try {
            Map<String, Object> tokenInfo = jwtService.getTokenInfo(token);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "tokenInfo", tokenInfo
            ));
            
        } catch (Exception e) {
            log.error("Error obteniendo info del token: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error obteniendo info del token: " + e.getMessage()
            ));
        }
    }
}
