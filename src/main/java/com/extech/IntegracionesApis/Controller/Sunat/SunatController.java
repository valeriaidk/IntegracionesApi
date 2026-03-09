package com.extech.IntegracionesApis.Controller.Sunat;

import com.extech.IntegracionesApis.Service.Sunat.SunatService;
import com.extech.IntegracionesApis.Domain.Dto.Sunat.SunatResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sunat")
@CrossOrigin(origins = "*")
@Tag(name = "Sunat", description = "Endpoints para consulta de datos con SUNAT")
public class SunatController {

    @Autowired
    private SunatService sunatService;

    @GetMapping("/consultar/{numero}")
    @Operation(summary = "Consulta RUC", description = "Consulta información de un RUC")
    public ResponseEntity<?> consultarRUC(@PathVariable String numero) {
        try {
            SunatResponse response = sunatService.consultarRUC(numero);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}