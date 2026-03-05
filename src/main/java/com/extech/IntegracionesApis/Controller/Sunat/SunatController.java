package com.extech.IntegracionesApis.Controller.Sunat;

import com.extech.IntegracionesApis.Service.Sunat.SunatService;
import com.extech.IntegracionesApis.Domain.Dto.Sunat.SunatResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sunat")
@CrossOrigin(origins = "*")
public class SunatController {

    @Autowired
    private SunatService sunatService;

    @GetMapping("/consultar/{numero}")
    public ResponseEntity<?> consultarRUC(@PathVariable String numero) {
        try {
            SunatResponse response = sunatService.consultarRUC(numero);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error: " + e.getMessage());
        }
    }
}