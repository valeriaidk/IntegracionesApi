package com.extech.IntegracionesApis.Controller;

import com.extech.IntegracionesApis.Repository.UserRepository;
import com.extech.IntegracionesApis.Service.JWT.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:58918", "http://127.0.0.1:56114"}, allowCredentials = "false")
public class DebugController {
    
    private final UserRepository userRepository;
    private final JwtService jwtService;
    
    @GetMapping("/user-tokens/{username}")
    public ResponseEntity<Map<String, Object>> getUserTokens(@PathVariable String username) {
        try {
            var user = userRepository.findActiveByUsername(username);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            var userData = user.get();
            
            return ResponseEntity.ok(Map.of(
                "username", userData.getUsername(),
                "lastToken", userData.getLastToken(),
                "apiToken", userData.getApiToken(),
                "planType", userData.getPlanType(),
                "lastLogin", userData.getLastLogin(),
                "createdAt", userData.getCreatedAt(),
                "updatedAt", userData.getUpdatedAt()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
    
    @PostMapping("/validate-db-token")
    public ResponseEntity<Map<String, Object>> validateDbToken(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String token = request.get("token");
            
            var user = userRepository.findActiveByUsername(username);
            
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            var userData = user.get();
            
            // Verificar si el token coincide con el guardado en BD
            boolean tokenMatches = token.equals(userData.getLastToken());
            
            // Validar el token JWT
            boolean jwtValid = jwtService.validateToken(token, username);
            
            return ResponseEntity.ok(Map.of(
                "username", username,
                "tokenMatchesDb", tokenMatches,
                "jwtValid", jwtValid,
                "dbToken", userData.getLastToken(),
                "providedToken", token,
                "planType", userData.getPlanType()
            ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", e.getMessage()
            ));
        }
    }
}
