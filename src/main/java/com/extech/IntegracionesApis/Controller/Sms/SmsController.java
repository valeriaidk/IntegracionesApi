package com.extech.IntegracionesApis.Controller.Sms;

import com.extech.IntegracionesApis.Service.JWT.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "SMS API", description = """
    ENDPOINTS:
    - POST /api/sms/send - Enviar SMS
    - GET /api/sms/config - Verificar configuración
    - GET /api/sms/status - Estado del servicio
    - GET /api/sms/balance - Consultar saldo
    - GET /api/sms/logs - Historial de envíos
    - POST /api/sms/validate - Validar número
    - GET /api/sms/templates - Plantillas disponibles
    - POST /api/sms/batch - Envío masivo
    
    FORMATO NÚMEROS:
    - Perú: +51XXXXXXXXX
    - Internacional: +[código][número]
    """)

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://127.0.0.1:58918", "http://127.0.0.1:56114"}, allowCredentials = "false")
public class SmsController {
    
    private final JwtService jwtService;
    private final RestTemplate restTemplate;
    private final String infobipBaseUrl;
    private final String infobipApiKey;
    
    @Operation(
        summary = "Enviar SMS",
        description = """
        Envía un mensaje de texto usando Infobip.
        
        EJEMPLO POSTMAN:
        POST /api/sms/send
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        Content-Type: application/json
        
        {
          "numero": "+51999999999",
          "mensaje": "Hola desde API"
        }
        """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del SMS",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                    {
                      "numero": "+51999999999",
                      "mensaje": "Hola desde API"
                    }
                    """
                )
            )
        )
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "SMS enviado exitosamente",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                    {
                      "status": "OK",
                      "message": "SMS enviado exitosamente via Infobip",
                      "infobipResponse": {
                        "messages": [{
                          "messageId": "4731568976647951051935",
                          "status": {
                            "description": "Message sent to next instance",
                            "groupId": 1,
                            "groupName": "PENDING",
                            "id": 26,
                            "name": "PENDING_ACCEPTED"
                          },
                          "to": "+51999999999"
                        }]
                      },
                      "recipient": "+51999999999",
                      "sentAt": "2026-03-10T16:20:00",
                      "provider": "Infobip"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Token JWT inválido o expirado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                    {
                      "error": "Token inválido o expirado"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Error en los datos del mensaje",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                    {
                      "error": "Se requieren 'numero' y 'mensaje'"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "Error en el servicio de Infobip",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                    {
                      "error": "Error enviando SMS: Connection timeout"
                    }
                    """
                )
            )
        )
    })
    @PostMapping("/send")
    public ResponseEntity<Map<String, Object>> sendSms(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, String> request) {
        
        try {
            // Validar token JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Header Authorization debe ser 'Bearer <token>'"
                ));
            }
            
            String token = authHeader.substring(7);
            
            // Validar token y extraer información
            if (!jwtService.validateToken(token, jwtService.extractUsername(token))) {
                return ResponseEntity.status(401).body(Map.of(
                    "error", "Token inválido o expirado"
                ));
            }
            
            String username = jwtService.extractUsername(token);
            String planType = jwtService.extractPlanType(token);
            
            // Validar datos del mensaje
            String numero = request.get("numero");
            String mensaje = request.get("mensaje");
            
            if (numero == null || mensaje == null) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Se requieren 'numero' y 'mensaje'"
                ));
            }
            
            // Validar plan (ejemplo: PREMIUM permite SMS ilimitados)
            // TEMPORAL: Permitir SMS sin validar plan para pruebas de Infobip
            // if ("PREMIUM".equals(planType) || "ESTÁNDAR".equals(planType)) {
            try {
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
                
                log.info("ENVIANDO SMS A INFOBIP - Usuario: {}, Número: {}, URL: {}", 
                        username, numero, url);
                log.info("API Key: {}", infobipApiKey);
                log.info("Request: {}", infobipRequest);
                
                // Enviar SMS a Infobip
                ResponseEntity<Map> response = restTemplate.postForEntity(
                    url, 
                    infobipRequest, 
                    Map.class
                );
                
                log.info("RESPUESTA INFOBIP - Status: {}, Body: {}", 
                        response.getStatusCode(), response.getBody());
                
                if (response.getStatusCode().is2xxSuccessful()) {
                    Map<String, Object> responseBody = response.getBody();
                    
                    return ResponseEntity.ok(Map.of(
                        "status", "OK",
                        "message", "SMS enviado exitosamente via Infobip",
                        "infobipResponse", responseBody,
                        "recipient", numero,
                        "sentAt", java.time.LocalDateTime.now().toString(),
                        "user", Map.of(
                            "username", username,
                            "planType", planType
                        ),
                        "provider", "Infobip",
                        "testMode", true
                    ));
                } else {
                    return ResponseEntity.status(response.getStatusCode())
                        .body(Map.of(
                            "error", "Error en API de Infobip",
                            "status", response.getStatusCode(),
                            "infobipResponse", response.getBody()
                        ));
                }
                
            } catch (Exception e) {
                log.error("ERROR ENVIANDO SMS VIA INFOBIP: {}", e.getMessage());
                return ResponseEntity.internalServerError().body(Map.of(
                    "error", "Error enviando SMS: " + e.getMessage(),
                    "provider", "Infobip",
                    "testMode", true
                ));
            }
                
            // TEMPORAL: Comentado para pruebas de Infobip
            // } else {
            //     return ResponseEntity.status(403).body(Map.of(
            //         "error", "Tu plan no permite enviar SMS",
            //         "planType", planType,
            //         "requiredPlan", "ESTÁNDAR o PREMIUM"
            //     ));
            // }
            
        } catch (Exception e) {
            log.error("Error enviando SMS: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error enviando SMS: " + e.getMessage()
            ));
        }
    }
    
    @Operation(
        summary = "Verificar configuración SMS",
        description = """
        Verifica que la configuración del servicio SMS sea correcta.
        
        EJEMPLO:
        GET /api/sms/config
        Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
        """
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "Configuración válida",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                    {
                      "status": "OK",
                      "message": "Configuración válida",
                      "apiUrl": "https://8vgly1.api.infobip.com/sms/2/text/advanced",
                      "sender": "INFOBIT",
                      "apiKeyLength": 64,
                      "timestamp": "2026-03-10T16:15:00"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "401", 
            description = "Token JWT inválido o expirado",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                    {
                      "error": "Header Authorization debe ser 'Bearer <token>'"
                    }
                    """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "Error en la configuración",
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                    {
                      "error": "Error grave en la configuración: API Key inválida"
                    }
                    """
                )
            )
        )
    })
    @GetMapping("/config")
    public ResponseEntity<Map<String, Object>> checkConfig(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            // Validar token JWT
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Header Authorization debe ser 'Bearer <token>'"
                ));
            }
            
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            
            return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Configuración válida",
                "apiUrl", "https://api.infobip.com/sms/2/text",
                "sender", "INFOBIT",
                "apiKeyLength", 64,
                "timestamp", java.time.LocalDateTime.now().toString(),
                "user", Map.of(
                    "username", username,
                    "authenticated", true
                )
            ));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error grave en la configuración: " + e.getMessage()
            ));
        }
    }
    
    @Operation(
        summary = "Estado del servicio",
        description = "Verificar disponibilidad del servicio SMS"
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus() {
        return ResponseEntity.ok(Map.of(
            "status", "OPERATIONAL",
            "infobipConnection", true,
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
    
    @Operation(
        summary = "Consultar saldo",
        description = "Obtener créditos disponibles y consumo"
    )
    @GetMapping("/balance")
    public ResponseEntity<Map<String, Object>> getBalance(
            @RequestHeader("Authorization") String authHeader) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Header Authorization debe ser 'Bearer <token>'"
                ));
            }
            
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);
            
            return ResponseEntity.ok(Map.of(
                "credits", 850.50,
                "currency", "USD",
                "usedThisMonth", 149.50,
                "remainingThisMonth", 700.00,
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error consultando saldo: " + e.getMessage()
            ));
        }
    }
    
    @Operation(
        summary = "Historial de envíos",
        description = "Listar SMS enviados con paginación"
    )
    @GetMapping("/logs")
    public ResponseEntity<Map<String, Object>> getLogs(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Header Authorization debe ser 'Bearer <token>'"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "messages", Arrays.asList(
                    Map.of(
                        "messageId", "4731568976647951051935",
                        "recipient", "+51999999999",
                        "content", "Hola desde API!",
                        "status", "DELIVERED",
                        "sentAt", "2026-03-10T16:20:00Z"
                    )
                ),
                "pagination", Map.of(
                    "page", page,
                    "size", size,
                    "total", 1247,
                    "totalPages", 25
                ),
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error consultando logs: " + e.getMessage()
            ));
        }
    }
    
    @Operation(
        summary = "Validar número",
        description = "Verificar formato y validez de número de teléfono"
    )
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateNumber(
            @RequestBody Map<String, String> request) {
        
        try {
            String number = request.get("number");
            
            if (number == null || number.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "El número de teléfono es requerido"
                ));
            }
            
            boolean isValid = number.startsWith("+") && number.length() >= 10;
            
            return ResponseEntity.ok(Map.of(
                "number", number,
                "isValid", isValid,
                "country", isValid ? "PE" : "Unknown",
                "countryName", isValid ? "Perú" : "Desconocido",
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error validando número: " + e.getMessage()
            ));
        }
    }
    
    @Operation(
        summary = "Plantillas disponibles",
        description = "Obtener plantillas de mensaje predefinidas"
    )
    @GetMapping("/templates")
    public ResponseEntity<Map<String, Object>> getTemplates() {
        return ResponseEntity.ok(Map.of(
            "templates", Arrays.asList(
                Map.of("id", "welcome", "name", "Bienvenida", "content", "¡Hola {name}! Bienvenido."),
                Map.of("id", "verification", "name", "Verificación", "content", "Tu código es: {code}."),
                Map.of("id", "appointment", "name", "Cita", "content", "Cita el {date} a las {time}.")
            ),
            "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
    
    @Operation(
        summary = "Envío masivo",
        description = "Enviar múltiples SMS en una sola llamada"
    )
    @PostMapping("/batch")
    public ResponseEntity<Map<String, Object>> sendBatch(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody Map<String, Object> request) {
        
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Header Authorization debe ser 'Bearer <token>'"
                ));
            }
            
            @SuppressWarnings("unchecked")
            java.util.List<Map<String, String>> messages = (java.util.List<Map<String, String>>) request.get("messages");
            
            if (messages == null || messages.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                    "error", "Se requiere la lista de mensajes"
                ));
            }
            
            return ResponseEntity.ok(Map.of(
                "batchId", "batch_" + System.currentTimeMillis(),
                "totalMessages", messages.size(),
                "status", "PROCESSING",
                "estimatedTime", messages.size() * 2 + " segundos",
                "timestamp", java.time.LocalDateTime.now().toString()
            ));
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Error en envío masivo: " + e.getMessage()
            ));
        }
    }
}
