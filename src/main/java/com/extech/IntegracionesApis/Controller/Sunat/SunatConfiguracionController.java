package com.extech.IntegracionesApis.Controller.Sunat;

import com.extech.IntegracionesApis.Service.Sunat.SunatConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sunat/config")
@Tag(name = "Sunat Configuración", description = "Configuración de API Sunat")
@io.swagger.v3.oas.annotations.Hidden
public class SunatConfiguracionController {

    @Autowired
    private SunatConfiguracionService configuracionService;

    @PostMapping("/inicializar")
    @io.swagger.v3.oas.annotations.Hidden
    public ResponseEntity<String> inicializar() {
        try {
            configuracionService.inicializarConfiguracionSunat();
            return ResponseEntity.ok("✅ Configuración Sunat guardada (token encriptado)");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("❌ Error: " + e.getMessage());
        }
    }
}

