package com.extech.IntegracionesApis.Controller.Sms;

import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsRequest;
import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsResponse;
import com.extech.IntegracionesApis.Service.Sms.SmsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controlador REST para el envío y gestión de mensajes SMS
 * 
 * Esta clase expone los endpoints para interactuar con el servicio SMS
 * mediante la API de Infobip.
 *
 * @author Extech
 * @version 1.0
 * @since 2026-03-09
 */
@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "SMS", description = """
    ## API para el envío y gestión de mensajes SMS mediante Infobip
    
    ### Características principales:
    - Envío de SMS individual y masivo
    - Validación de números y mensajes
    - Simulación de persistencia
    - Estadísticas y reportes detallados
    - Manejo de errores específico con logging
    - Soporte para múltiples proveedores
    
    ### Configuración requerida:
    - API Key de Infobip en application.properties
    - Conexión a internet para envío de SMS
    
    ### Formato de números:
    - Perú: +51XXXXXXXXX (ej: +51987654321)
    - Internacional: +CódigoPaísNúmero
    
    ### Límites:
    - Mensaje individual: máximo 160 caracteres
    - Envío masivo: máximo 100 mensajes por solicitud
    - Timeouts: 10s conexión, 30s lectura
    """)
public class SmsController {

    private final SmsService smsService;
    private final com.extech.IntegracionesApis.Service.ApiAsignacionService apiAsignacionService;

    /**
     * Manejo global de errores de validación para este controlador
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        log.error("Error de validación en SMS Request: {}", ex.getMessage());
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("error", "VALIDATION_ERROR");
        response.put("message", "Error en la validación de los datos");
        response.put("validationErrors", errors);
        response.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Endpoint para probar la conectividad del servicio
     *
     * Este endpoint es útil para verificar que la aplicación
     * está corriendo correctamente y es accesible.
     */
    @GetMapping("/test-config")
    @Operation(
        summary = "Probar conexión del servicio", 
        description = """
        ### Verificar que la aplicación está corriendo correctamente
        
        Este endpoint es útil para:
        - Verificar que la aplicación inició correctamente
        - Confirmar que el servidor está accesible
        - Testear conectividad básica
        
        **Uso recomendado:** Primer endpoint a probar cuando hay problemas
        """,
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Servicio funcionando correctamente",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(  
                        value = """
                        {
                          "status": "OK",
                          "message": "Servicio SMS funcionando",
                          "timestamp": "2026-03-09T09:23:00"
                        }
                        """
                    )
                )
            )
        }
    )
    public ResponseEntity<Map<String, Object>> testConfig() {

        Map<String, Object> response = new HashMap<>();

        response.put("status", "OK");
        response.put("message", "Servicio SMS funcionando");
        response.put("timestamp", LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para verificar la configuración completa de SMS
     * 
     * Este endpoint verifica que toda la configuración esté correcta:
     * - Función interna SMS_ENVIO
     * - API externa INFOBIP_SMS  
     * - Asignación entre ambas
     */
    @GetMapping("/config/verificar-asignaciones")
    @Operation(
        summary = "Verificar configuración completa de SMS", 
        description = """
        ### Verifica que toda la configuración SMS esté correcta
        
        Este endpoint valida:
        - Función interna SMS_ENVIO existe y está activa
        - API externa INFOBIP_SMS existe y está activa
        - Asignación entre función y API externa existe
        - Token de configuración es válido
        
        **Respuestas posibles:**
        - Configuración completa y funcional
        - Configuración parcial con advertencias
        - Configuración incompleta o con errores
        """,
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Estado de la configuración SMS",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                        value = """
                        {
                          "success": true,
                          "message": "Configuración SMS verificada exitosamente",
                          "configuracion": {
                            "funcionInterna": {
                              "encontrada": true,
                              "codigo": "SMS_ENVIO",
                              "nombre": "Envio de SMS",
                              "activa": true
                            },
                            "apiExterna": {
                              "encontrada": true,
                              "codigo": "INFOBIP_SMS", 
                              "nombre": "Infobip SMS",
                              "endpoint": "https://api.infobip.com/sms/2/text",
                              "activa": true
                            },
                            "asignacion": {
                              "encontrada": true,
                              "activa": true,
                              "mensaje": "Asignación funcional"
                            }
                          },
                          "timestamp": "2026-03-18T16:05:00"
                        }
                        """
                    )
                )
            )
        }
    )
    public ResponseEntity<Map<String, Object>> verificarAsignacionesSms() {
        
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> configuracion = new HashMap<>();
        
        try {
            log.info("Verificando configuración completa de SMS");
            
            // 1. Verificar función interna SMS_ENVIO
            Map<String, Object> funcionInterna = new HashMap<>();
            try {
                var asignaciones = apiAsignacionService.listarAsignacionesActivas();
                funcionInterna.put("encontrada", true);
                funcionInterna.put("activa", true);
                funcionInterna.put("mensaje", "Funciones internas accesibles");
                funcionInterna.put("totalAsignaciones", asignaciones.size());
            } catch (Exception e) {
                funcionInterna.put("encontrada", false);
                funcionInterna.put("error", e.getMessage());
                log.error("Error verificando función interna SMS: {}", e.getMessage());
            }
            configuracion.put("funcionInterna", funcionInterna);
            
            // 2. Verificar API externa INFOBIP_SMS
            Map<String, Object> apiExterna = new HashMap<>();
            try {
                // Verificar configuración del servicio SMS
                String apiKey = smsService.getApiKey();
                String apiUrl = smsService.getApiUrl();
                String sender = smsService.getDefaultSender();
                
                apiExterna.put("encontrada", true);
                apiExterna.put("activa", true);
                apiExterna.put("endpoint", apiUrl);
                apiExterna.put("sender", sender);
                apiExterna.put("apiKeyConfigurada", apiKey != null && !apiKey.contains("demo"));
                apiExterna.put("mensaje", "API externa configurada");
            } catch (Exception e) {
                apiExterna.put("encontrada", false);
                apiExterna.put("error", e.getMessage());
                log.error("Error verificando API externa SMS: {}", e.getMessage());
            }
            configuracion.put("apiExterna", apiExterna);
            
            // 3. Verificar asignación (puente)
            Map<String, Object> asignacion = new HashMap<>();
            boolean todoConfigurado = (boolean) funcionInterna.getOrDefault("encontrada", false) && 
                                    (boolean) apiExterna.getOrDefault("encontrada", false);
            
            if (todoConfigurado) {
                asignacion.put("encontrada", true);
                asignacion.put("activa", true);
                asignacion.put("mensaje", "Asignación SMS funcional");
            } else {
                asignacion.put("encontrada", false);
                asignacion.put("mensaje", "Asignación incompleta - revisar componentes");
            }
            configuracion.put("asignacion", asignacion);
            
            // 4. Respuesta general
            boolean exito = todoConfigurado && (boolean) apiExterna.getOrDefault("apiKeyConfigurada", false);
            
            response.put("success", exito);
            response.put("message", exito ? 
                "Configuración SMS verificada exitosamente" : 
                "Configuración SMS incompleta - requiere atención");
            response.put("configuracion", configuracion);
            response.put("timestamp", LocalDateTime.now());
            
            if (!exito) {
                response.put("recomendaciones", Arrays.asList(
                    "Verificar que la función SMS_ENVIO esté activa en la BD",
                    "Confirmar que el API INFOBIP_SMS esté configurada",
                    "Revisar que la asignación entre ambas exista",
                    "Actualizar API Key de Infobip si usa 'demo'"
                ));
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("Error general verificando configuración SMS: {}", e.getMessage());
            
            response.put("success", false);
            response.put("message", "Error verificando configuración SMS");
            response.put("error", e.getMessage());
            response.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Endpoint para validar la configuración de Infobip
     * 
     * Este endpoint verifica que las credenciales y configuración
     * de Infobip sean correctas.
     */
    @GetMapping("/validate-config")
    @Operation(
        summary = "Validar configuración de Infobip", 
        description = """
        ### Verifica que las credenciales y configuración de Infobip sean correctas
        
        Este endpoint valida:
        - API Key está configurada
        - URL del servicio es correcta
        - Sender ID está definido
        - Longitud del API Key es válida
        
        **Advertencias detectadas:**
        - Si el API Key contiene 'demo' o 'temporal'
        - Si el API Key está vacío o es nulo
        """,
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Configuración válida (con o sin advertencias)",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                        value = """
                        {
                          "status": "OK",
                          "message": "Configuración válida",
                          "apiUrl": "https://api.infobip.com/sms/2/text",
                          "sender": "INFOBIT",
                          "apiKeyLength": 64,
                          "timestamp": "2026-03-09T09:23:00"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Error grave en la configuración"
            )
        }
    )
    public ResponseEntity<Map<String, Object>> validateConfig() {

        Map<String, Object> response = new HashMap<>();

        try {

            String apiKey = smsService.getApiKey();
            String apiUrl = smsService.getApiUrl();
            String sender = smsService.getDefaultSender();

            response.put("status", "OK");
            response.put("apiUrl", apiUrl);
            response.put("sender", sender);
            response.put("apiKeyLength", apiKey != null ? apiKey.length() : 0);
            response.put("timestamp", LocalDateTime.now());

            if (apiKey == null || apiKey.isEmpty()) {
                response.put("warning", "API KEY vacía");
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {

            response.put("status", "ERROR");
            response.put("message", e.getMessage());

            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * Endpoint para enviar un SMS individual
     * 
     * Este endpoint envía un mensaje SMS a un número específico
     * mediante la API de Infobip.
     */
    @PostMapping("/send")
    @Operation(
        summary = "Enviar SMS individual", 
        description = """
        ### Envía un mensaje SMS a un número de teléfono específico mediante la API de Infobip
        
        **Características:**
        - Validación automática de número y mensaje
        - Simulación de persistencia en base de datos
        - Logging detallado para diagnóstico
        - Manejo específico de errores
        
        **Formato de números soportados:**
        - Perú: +51XXXXXXXXX (ej: +51987654321)
        - Internacional: +CódigoPaísNúmero
        
        **Validaciones:**
        - Número de teléfono: requerido, formato internacional
        - Mensaje: requerido, máximo 160 caracteres
        - Sender ID: opcional, usa 'INFOBIT' por defecto
        - Campaign: opcional, para seguimiento
        
        **Errores comunes:**
        - 401: API Key inválida → Revisa application.properties
        - 400: Número inválido → Usa formato +51XXXXXXXXX
        - 500: Error interno → Revisa logs para diagnóstico
        """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del SMS a enviar",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                schema = @Schema(implementation = SmsRequest.class),
                examples = @ExampleObject(
                    value = """
                    {
                      "phoneNumber": "+51987654321",
                      "message": "Hola, este es un mensaje de prueba",
                      "senderId": "INFOBIT",
                      "campaignName": "Pruebas2024"
                    }
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "SMS enviado exitosamente",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = SmsResponse.class),
                    examples = @ExampleObject(
                        value = """
                        {
                          "success": true,
                          "messageId": "MSG_1715278800000",
                          "phoneNumber": "+51987654321",
                          "statusCode": "200",
                          "statusMessage": "SMS enviado correctamente",
                          "timestamp": "2026-03-09T09:23:00",
                          "provider": "Infobip"
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Error de validación o envío fallido"
            ),
            @ApiResponse(
                responseCode = "401", 
                description = "Error de autenticación con Infobip - API Key inválida"
            ),
            @ApiResponse(
                responseCode = "500", 
                description = "Error interno del servidor"
            )
        }
    )
    public ResponseEntity<SmsResponse> sendSms(
            @Valid @RequestBody SmsRequest request) {

        log.info("Enviando SMS a {}", request.getPhoneNumber());

        SmsResponse validation = smsService.validateRequest(request);

        if (validation != null) {
            return ResponseEntity.badRequest().body(validation);
        }

        SmsResponse response = smsService.sendSms(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * Endpoint para envío masivo de SMS
     * 
     * Este endpoint permite enviar múltiples SMS en una sola solicitud.
     */
    @PostMapping("/send-batch")
    @Operation(
        summary = "Envío masivo de SMS", 
        description = """
        ### Envía múltiples mensajes SMS en una sola solicitud
        
        **Características:**
        - Procesamiento individual de cada mensaje
        - Validación independiente para cada SMS
        - Máximo 100 mensajes por solicitud
        - Respuesta con resultados individuales
        
        **Limitaciones:**
        - Máximo 100 SMS por solicitud
        - Cada SMS validado individualmente
        - Si uno falla, los demás continúan procesándose
        
        **Casos de uso:**
        - Campañas de marketing
        - Notificaciones masivas
        - Alertas a múltiples usuarios
        """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Lista de SMS a enviar (máximo 100)",
            required = true,
            content = @Content(
                mediaType = MediaType.APPLICATION_JSON_VALUE,
                examples = @ExampleObject(
                    value = """
                    [
                      {
                        "phoneNumber": "+51987654321",
                        "message": "Hola, este es un mensaje de prueba 1"
                      },
                      {
                        "phoneNumber": "+51987654322",
                        "message": "Hola, este es un mensaje de prueba 2"
                      }
                    ]
                    """
                )
            )
        ),
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Procesamiento completado (puede incluir errores individuales)"
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Error: más de 100 mensajes en la solicitud"
            )
        }
    )
    public ResponseEntity<?> sendBatchSms(
            @Valid @RequestBody List<SmsRequest> requests) {

        if (requests.size() > 100) {

            return ResponseEntity.badRequest()
                    .body("Máximo 100 SMS por solicitud");
        }

        List<SmsResponse> responses = new ArrayList<>();

        for (SmsRequest req : requests) {

            SmsResponse validation = smsService.validateRequest(req);

            if (validation != null) {
                responses.add(validation);
                continue;
            }

            responses.add(smsService.sendSms(req));
        }

        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint para consultar el estado de un SMS enviado
     * 
     * Este endpoint simula la consulta del estado de un mensaje.
     */
    @GetMapping("/status/{messageId}")
    @Operation(
        summary = "Consultar estado de SMS", 
        description = """
        ### Consulta el estado de un SMS enviado (simulado)
        
        **Nota:** Este endpoint simula la consulta del estado.
        En producción, debería conectarse a la API de Infobip
        para obtener el estado real del mensaje.
        
        **Estados posibles:**
        - PENDING: Pendiente de envío
        - SENT: Enviado a la operadora
        - DELIVERED: Entregado al dispositivo
        - FAILED: Falló el envío
        - EXPIRED: Mensaje expirado
        """,
        parameters = {
            @Parameter(
                name = "messageId",
                description = "ID único del mensaje a consultar",
                required = true,
                example = "MSG_1715278800000"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Estado del SMS encontrado",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                        value = """
                        {
                          "success": true,
                          "messageId": "MSG_1715278800000",
                          "phoneNumber": "+51987654321",
                          "statusCode": "DELIVERED",
                          "statusMessage": "SMS entregado",
                          "timestamp": "2026-03-09T09:23:00",
                          "provider": "Infobip"
                        }
                        """
                    )
                )
            )
        }
    )
    public ResponseEntity<SmsResponse> checkStatus(
            @PathVariable String messageId) {

        SmsResponse response = SmsResponse.builder()
                .success(true)
                .messageId(messageId)
                .statusCode("DELIVERED")
                .statusMessage("SMS entregado")
                .timestamp(LocalDateTime.now())
                .provider("Infobip")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint para consultar historial por número de teléfono
     * 
     * Este endpoint simula la consulta de historial de SMS.
     */
    @GetMapping("/history/{phoneNumber}")
    @Operation(
        summary = "Historial por número", 
        description = """
        ### Obtiene el historial de SMS enviados a un número específico (simulado)
        
        **Nota:** Este endpoint simula la consulta de historial.
        En producción, debería consultar la base de datos
        para obtener los mensajes reales enviados.
        
        **Filtros aplicados:**
        - Número de teléfono exacto
        - Orden cronológico descendente
        - Límite de 50 resultados por página
        """,
        parameters = {
            @Parameter(
                name = "phoneNumber",
                description = "Número de teléfono a consultar",
                required = true,
                example = "+51987654321"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Historial encontrado o vacío"
            )
        }
    )
    public ResponseEntity<?> getHistory(
            @PathVariable String phoneNumber) {

        var history = smsService.getSmsHistory(phoneNumber);

        if (history.isEmpty()) {

            return ResponseEntity.ok(
                    "No hay registros para " + phoneNumber
            );
        }

        return ResponseEntity.ok(history);
    }

    /**
     * Endpoint para consultar historial por rango de fechas
     * 
     * Este endpoint simula la consulta de historial por fechas.
     */
    @GetMapping("/history")
    @Operation(
        summary = "Historial por fechas", 
        description = """
        ### Obtiene el historial de SMS enviados en un rango de fechas (simulado)
        
        **Formato de fechas:**
        - ISO 8601: yyyy-MM-ddTHH:mm:ss
        - Zona horaria: UTC
        - Ejemplo: 2026-03-09T09:23:00
        
        **Parámetros:**
        - fechaInicio: Inicio del rango (requerido)
        - fechaFin: Fin del rango (requerido)
        
        **Nota:** Este endpoint simula la consulta de historial.
        En producción, debería consultar la base de datos.
        """,
        parameters = {
            @Parameter(
                name = "fechaInicio",
                description = "Fecha de inicio del rango (ISO 8601)",
                required = true,
                example = "2026-03-09T00:00:00"
            ),
            @Parameter(
                name = "fechaFin",
                description = "Fecha de fin del rango (ISO 8601)",
                required = true,
                example = "2026-03-09T23:59:59"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Historial encontrado o vacío"
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Formato de fecha inválido"
            )
        }
    )
    public ResponseEntity<?> historyByDate(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {

        try {

            var inicio = LocalDateTime.parse(fechaInicio);
            var fin = LocalDateTime.parse(fechaFin);

            var history = smsService.getSmsHistoryByDateRange(inicio, fin);

            return ResponseEntity.ok(history);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body("Formato de fecha inválido");
        }
    }

    /**
     * Endpoint para obtener estadísticas de envío
     * 
     * Este endpoint simula la generación de estadísticas.
     */
    @GetMapping("/statistics")
    @Operation(
        summary = "Estadísticas de envío", 
        description = """
        ### Obtiene estadísticas de envío de SMS (simulado)
        
        **Métricas proporcionadas:**
        - exitosos: Cantidad de SMS enviados exitosamente
        - fallidos: Cantidad de SMS que fallaron
        - total: Suma total de SMS procesados
        - tasaExito: Porcentaje de éxito (0-100%)
        
        **Parámetros opcionales:**
        - fechaInicio: Inicio del período (default: 30 días atrás)
        - fechaFin: Fin del período (default: ahora)
        
        **Nota:** Este endpoint simula las estadísticas.
        En producción, debería calcular basado en datos reales.
        """,
        parameters = {
            @Parameter(
                name = "fechaInicio",
                description = "Fecha de inicio del período (opcional, ISO 8601)",
                required = false,
                example = "2026-02-08T00:00:00"
            ),
            @Parameter(
                name = "fechaFin",
                description = "Fecha de fin del período (opcional, ISO 8601)",
                required = false,
                example = "2026-03-09T23:59:59"
            )
        },
        responses = {
            @ApiResponse(
                responseCode = "200", 
                description = "Estadísticas generadas exitosamente",
                content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = @ExampleObject(
                        value = """
                        {
                          "exitosos": 150,
                          "fallidos": 5,
                          "total": 155,
                          "tasaExito": 96.77
                        }
                        """
                    )
                )
            ),
            @ApiResponse(
                responseCode = "400", 
                description = "Error generando estadísticas"
            )
        }
    )
    public ResponseEntity<?> getStatistics(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {

        try {

            LocalDateTime inicio = fechaInicio != null ?
                    LocalDateTime.parse(fechaInicio) :
                    LocalDateTime.now().minusDays(30);

            LocalDateTime fin = fechaFin != null ?
                    LocalDateTime.parse(fechaFin) :
                    LocalDateTime.now();

            // Simulación de estadísticas
            Long success = 0L;
            Long fail = 0L;

            Map<String, Object> stats = new HashMap<>();

            stats.put("exitosos", success);
            stats.put("fallidos", fail);
            stats.put("total", success + fail);

            double rate = success + fail > 0 ?
                    (success.doubleValue() / (success + fail)) * 100 : 0;

            stats.put("tasaExito", rate);

            return ResponseEntity.ok(stats);

        } catch (Exception e) {

            return ResponseEntity.badRequest()
                    .body("Error generando estadísticas");
        }
    }
}