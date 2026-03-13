package com.extech.IntegracionesApis.Controller.Reniec;

import com.extech.IntegracionesApis.Service.Reniec.ReniecService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reniec")
@CrossOrigin(origins = "*")
@Tag(name = "Reniec", description = "Endpoints para consulta de datos con RENIEC")
public class ReniecController {

    @Autowired
    private ReniecService reniecService;

    // GET /api/reniec/consultar/DNI/71234567
    @GetMapping("/consultar/{tipo}/{numero}")
    @Operation(
            summary = "Consultar documento en RENIEC",
            description = "Permite consultar información de DNI o RUC a través del servicio de RENIEC"
    )
    public ResponseEntity<?> consultarDocumento(
            @PathVariable String tipo,
            @PathVariable String numero) {
        try {
            if ("DNI".equalsIgnoreCase(tipo)) {
                return ResponseEntity.ok(reniecService.consultarDNI(numero));
            } else if ("RUC".equalsIgnoreCase(tipo)) {
                return ResponseEntity.ok(reniecService.consultarRUC(numero));
            } else {
                return ResponseEntity.badRequest().body("Tipo de documento inválido");
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}