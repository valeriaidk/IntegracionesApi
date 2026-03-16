package com.extech.IntegracionesApis.Controller;

import com.extech.IntegracionesApis.Service.Auth.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, Object> body) {
        String usuario = body.get("usuario") != null ? body.get("usuario").toString() : null;
        String contrasena = body.get("contrasena") != null ? body.get("contrasena").toString() : null;

        return ResponseEntity.ok(authService.autenticar(usuario, contrasena));
    }

    @PostMapping("/actualizar")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> body) {
        String email = body.get("email") != null ? body.get("email").toString() : null;
        String nuevaPassword = body.get("nuevaPassword") != null ? body.get("nuevaPassword").toString() : null;

        return ResponseEntity.ok(authService.actualizarPassword(email, nuevaPassword));
    }
}
