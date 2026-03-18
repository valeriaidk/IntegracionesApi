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
        String email = body.get("email") != null ? body.get("email").toString() : null;
        String password = body.get("password") != null ? body.get("password").toString() : null;

        return ResponseEntity.ok(authService.autenticar(email, password));
    }

    @PostMapping("/generate-token")
    public ResponseEntity<?> generateToken(@RequestBody Map<String, Object> body) {
        Integer usuarioId = body.get("usuarioId") != null ? ((Number) body.get("usuarioId")).intValue() : null;
        if (usuarioId == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "error", "usuarioId es requerido"
            ));
        }

        return ResponseEntity.ok(authService.regenerarApiKey(usuarioId));
    }

    @PostMapping("/actualizar")
    public ResponseEntity<?> actualizarUsuario(@RequestBody Map<String, Object> body) {
        Integer usuarioId = body.get("usuarioId") != null ? ((Number) body.get("usuarioId")).intValue() : null;
        String nombre = body.get("nombre") != null ? body.get("nombre").toString() : null;
        String apellido = body.get("apellido") != null ? body.get("apellido").toString() : null;
        String email = body.get("email") != null ? body.get("email").toString() : null;
        String password = body.get("password") != null ? body.get("password").toString() : null;
        Integer planId = body.get("planId") != null ? ((Number) body.get("planId")).intValue() : null;
        String telefono = body.get("telefono") != null ? body.get("telefono").toString() : null;
        String razonSocial = body.get("razonSocial") != null ? body.get("razonSocial").toString() : null;
        String ruc = body.get("ruc") != null ? body.get("ruc").toString() : null;
        Boolean activo = body.get("activo") != null ? Boolean.parseBoolean(body.get("activo").toString()) : null;
        Boolean eliminado = body.get("eliminado") != null ? Boolean.parseBoolean(body.get("eliminado").toString()) : null;
        Integer usuarioAccion = body.get("usuarioAccion") != null ? ((Number) body.get("usuarioAccion")).intValue() : null;

        return ResponseEntity.ok(authService.actualizarUsuario(usuarioId, nombre, apellido, email, password, planId, telefono, razonSocial, ruc, activo, eliminado, usuarioAccion));
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, Object> body) {
        String nombre = body.get("nombre") != null ? body.get("nombre").toString() : null;
        String apellido = body.get("apellido") != null ? body.get("apellido").toString() : null;
        String email = body.get("email") != null ? body.get("email").toString() : null;
        String password = body.get("password") != null ? body.get("password").toString() : null;
        Integer planId = body.get("planId") != null ? ((Number) body.get("planId")).intValue() : null;
        String telefono = body.get("telefono") != null ? body.get("telefono").toString() : null;
        String razonSocial = body.get("razonSocial") != null ? body.get("razonSocial").toString() : null;
        String ruc = body.get("ruc") != null ? body.get("ruc").toString() : null;
        Boolean activo = body.get("activo") != null ? Boolean.parseBoolean(body.get("activo").toString()) : null;
        Boolean eliminado = body.get("eliminado") != null ? Boolean.parseBoolean(body.get("eliminado").toString()) : null;
        Integer usuarioAccion = body.get("usuarioAccion") != null ? ((Number) body.get("usuarioAccion")).intValue() : null;

        return ResponseEntity.ok(authService.registrarUsuario(nombre, apellido, email, password, planId, telefono, razonSocial, ruc, activo, eliminado, usuarioAccion));
    }

    @PutMapping("/usuario")
    public ResponseEntity<?> updateUsuario(@RequestBody Map<String, Object> body) {
        Integer usuarioId = body.get("usuarioId") != null ? ((Number) body.get("usuarioId")).intValue() : null;
        String nombre = body.get("nombre") != null ? body.get("nombre").toString() : null;
        String apellido = body.get("apellido") != null ? body.get("apellido").toString() : null;
        String email = body.get("email") != null ? body.get("email").toString() : null;
        String password = body.get("password") != null ? body.get("password").toString() : null;
        Integer planId = body.get("planId") != null ? ((Number) body.get("planId")).intValue() : null;
        String telefono = body.get("telefono") != null ? body.get("telefono").toString() : null;
        String razonSocial = body.get("razonSocial") != null ? body.get("razonSocial").toString() : null;
        String ruc = body.get("ruc") != null ? body.get("ruc").toString() : null;
        Boolean activo = body.get("activo") != null ? Boolean.parseBoolean(body.get("activo").toString()) : null;
        Boolean eliminado = body.get("eliminado") != null ? Boolean.parseBoolean(body.get("eliminado").toString()) : null;
        Integer usuarioAccion = body.get("usuarioAccion") != null ? ((Number) body.get("usuarioAccion")).intValue() : null;

        return ResponseEntity.ok(authService.actualizarUsuario(usuarioId, nombre, apellido, email, password, planId, telefono, razonSocial, ruc, activo, eliminado, usuarioAccion));
    }
}
