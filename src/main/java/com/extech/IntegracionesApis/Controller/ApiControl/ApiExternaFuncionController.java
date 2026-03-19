package com.extech.IntegracionesApis.Controller.ApiControl;

import com.extech.IntegracionesApis.Service.ApiExterna.ApiExternaFuncionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/apis-externas")
@RequiredArgsConstructor
public class ApiExternaFuncionController {

    private final ApiExternaFuncionService apiExternaFuncionService;

    @PostMapping("/guardar")
    public ResponseEntity<?> guardarApiExterna(@RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(apiExternaFuncionService.guardarApiExterna(body));
    }

    @GetMapping("/listar")
    public ResponseEntity<?> listarApisActivas() {
        return ResponseEntity.ok(apiExternaFuncionService.listarApisActivas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerApi(@PathVariable Integer id) {
        return ResponseEntity.ok(apiExternaFuncionService.obtenerApiConTokenDescifrado(id));
    }

    @GetMapping("/{id}/token")
    public ResponseEntity<?> obtenerToken(@PathVariable Integer id) {
        String token = apiExternaFuncionService.obtenerTokenDescifrado(id);
        if (token == null) {
            return ResponseEntity.ok(Map.of(
                "message", "La API no tiene token configurado",
                "token", null
            ));
        }
        return ResponseEntity.ok(Map.of(
            "message", "Token descifrado exitosamente",
            "token", token,
            "nota", "Usar este token solo en memoria para consumo HTTP"
        ));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarApiExterna(
            @PathVariable Integer id,
            @RequestBody Map<String, Object> body) {
        return ResponseEntity.ok(apiExternaFuncionService.actualizarApiExterna(id, body));
    }
}
