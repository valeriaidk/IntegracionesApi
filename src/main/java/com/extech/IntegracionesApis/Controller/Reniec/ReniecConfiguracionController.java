package com.extech.IntegracionesApis.Controller.Reniec;

import com.extech.IntegracionesApis.Service.Reniec.ReniecConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reniec/config")
@Tag(name = "Reniec Configuración", description = "Configuración de API Reniec")
@io.swagger.v3.oas.annotations.Hidden
public class ReniecConfiguracionController {

    @Autowired
    private ReniecConfiguracionService configuracionService;

    @PostMapping("/inicializar")
    @io.swagger.v3.oas.annotations.Hidden
    public ResponseEntity<String> inicializar() {
        try {
            configuracionService.inicializarConfiguracionReniec();
            return ResponseEntity.ok("✅ Configuración Reniec guardada (token encriptado)");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error: " + e.getMessage());
        }
    }
}
