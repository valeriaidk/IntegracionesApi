package com.extech.IntegracionesApis.Controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:58918", "http://127.0.0.1:56114"}, allowCredentials = "false")
public class TestInfobipController {
    
    private final RestTemplate restTemplate;
    private final String infobipBaseUrl;
    private final String infobipApiKey;
    
    @PostMapping("/infobip-sms")
    public ResponseEntity<Map<String, Object>> testInfobip(@RequestBody Map<String, String> request) {
        try {
            String numero = request.get("numero");
            String mensaje = request.get("mensaje");
            
            log.info("=== PRUEBA INFOBIP ===");
            log.info("URL Base: {}", infobipBaseUrl);
            log.info("API Key: {}", infobipApiKey);
            log.info("Número: {}", numero);
            log.info("Mensaje: {}", mensaje);
            
            // Construir request para Infobip API
            Map<String, Object> infobipRequest = Map.of(
                "messages", new Object[]{
                    Map.of(
                        "from", "INFOBIT",
                        "destinations", new Object[]{
                            Map.of("to", numero)
                        },
                        "text", mensaje
                    )
                }
            );
            
            String url = infobipBaseUrl + "/sms/2/text/advanced";
            
            log.info("URL completa: {}", url);
            log.info("Request body: {}", infobipRequest);
            
            // Enviar SMS a Infobip
            ResponseEntity<Map> response = restTemplate.postForEntity(
                url, 
                infobipRequest, 
                Map.class
            );
            
            log.info("Status Code: {}", response.getStatusCode());
            log.info("Response Body: {}", response.getBody());
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Prueba completada",
                "statusCode", response.getStatusCode().toString(),
                "response", response.getBody(),
                "url", url,
                "apiKey", infobipApiKey.substring(0, 10) + "..."
            ));
            
        } catch (Exception e) {
            log.error("ERROR EN PRUEBA INFOBIP: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "error", e.getMessage(),
                "stackTrace", e.getStackTrace()[0].toString()
            ));
        }
    }
}
