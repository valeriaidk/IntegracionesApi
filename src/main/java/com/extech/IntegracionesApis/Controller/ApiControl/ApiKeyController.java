package com.extech.IntegracionesApis.Controller.ApiControl;

import com.extech.IntegracionesApis.Service.Auth.ApiKeyService;
import com.extech.IntegracionesApis.Util.Security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/apikey")
@RequiredArgsConstructor
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    @PostMapping("/generar")
    public ResponseEntity<?> generarApiKey() {
        Integer usuarioId = UserContext.getUsuarioId();
        if (usuarioId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error",
                        "Sesión inválida. Inicia sesión nuevamente."));
        }
        try {
            Map<String, Object> resultado = apiKeyService.generarApiKey(usuarioId);
            return ResponseEntity.ok(resultado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/estado")
    public ResponseEntity<?> estadoApiKey() {
        Integer usuarioId = UserContext.getUsuarioId();
        if (usuarioId == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("error",
                        "Sesión inválida. Inicia sesión nuevamente."));
        }
        Map<String, Object> resultado = apiKeyService.estadoApiKey(usuarioId);
        return ResponseEntity.ok(resultado);
    }
}
