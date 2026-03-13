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
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:58918", "http://127.0.0.1:56114"}, allowCredentials = "false")
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
            
            String token = jwtService.generateToken(username, password, "PREMIUM");
            Map<String, Object> tokenInfo = jwtService.getTokenInfo(token);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "token", token,
                "tokenInfo", tokenInfo,
                "usage", "Usa este token en el header Authorization: Bearer <token>",
                "postman", Map.of(
                    "type", "Bearer Token",
                    "token", token,
                    "prefix", "Bearer",
                    "header", "Authorization"
                )
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error generando token: " + e.getMessage()
            ));
        }
    }
}
