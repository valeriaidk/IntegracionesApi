package com.extech.IntegracionesApis.Service.Sms;

import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsRequest;
import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsResponse;
import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiServicesFuncion;
import com.extech.IntegracionesApis.Service.ApiResolucionService;
import com.extech.IntegracionesApis.Service.AuditoriaService;
import com.extech.IntegracionesApis.Util.Security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Servicio para el envío de SMS mediante configuración dinámica de la base de datos
 * 
 * Esta clase maneja la lógica de negocio para enviar mensajes SMS,
 * utilizando la configuración resuelta desde la base de datos a través
 * del flujo: TokenUsuario -> Usuario -> Función -> SP -> Configuración Externa
 * 
 * @author Extech
 * @version 2.0
 * @since 2026-03-16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final RestTemplate restTemplate;
    private final ApiResolucionService apiResolucionService;
    private final AuditoriaService auditoriaService;

    /**
     * Código de función interna para SMS
     */
    private static final String CODIGO_FUNCION_SMS = "SMS_SEND";

    /**
     * Envía un mensaje SMS usando configuración resuelta desde base de datos
     * 
     * Este método implementa el flujo completo:
     * 1. Obtiene usuario autenticado del contexto
     * 2. Resuelve configuración externa usando el SP
     * 3. Descifra token del proveedor si aplica
     * 4. Construye y envía request al proveedor real
     * 5. Registra auditoría del consumo
     * 
     * @param request Objeto con los datos del SMS a enviar
     * @return SmsResponse con el resultado del envío
     */
    public SmsResponse sendSms(SmsRequest request) {

        Integer usuarioId = UserContext.getUsuarioId();
        if (usuarioId == null) {
            log.error("No hay usuario autenticado en el contexto");
            return SmsResponse.builder()
                    .success(false)
                    .phoneNumber(request.getPhoneNumber())
                    .errorCode("AUTH_ERROR")
                    .errorMessage("Usuario no autenticado")
                    .statusCode("401")
                    .statusMessage("Error de autenticación")
                    .timestamp(java.time.LocalDateTime.now())
                    .provider("Sistema")
                    .build();
        }

        log.info("Enviando SMS para usuarioId: {} a {}", usuarioId, request.getPhoneNumber());

        try {
            // 1. Obtener función interna
            Optional<ApiServicesFuncion> funcionOpt = apiResolucionService.obtenerFuncionInterna(CODIGO_FUNCION_SMS);
            if (funcionOpt.isEmpty()) {
                log.error("Función interna no encontrada: {}", CODIGO_FUNCION_SMS);
                return crearRespuestaError(request, "FUNCION_NO_ENCONTRADA", "Función SMS no configurada", usuarioId, null);
            }

            ApiServicesFuncion funcion = funcionOpt.get();

            // 2. Resolver configuración externa usando el SP
            Optional<ApiExternaFuncion> configOpt = apiResolucionService.resolverConfiguracionExterna(usuarioId, CODIGO_FUNCION_SMS);
            if (configOpt.isEmpty()) {
                log.error("Configuración externa no encontrada para usuarioId: {} y función: {}", usuarioId, CODIGO_FUNCION_SMS);
                return crearRespuestaError(request, "CONFIG_NO_ENCONTRADA", "Configuración de proveedor SMS no encontrada", usuarioId, funcion.getApiServicesFuncionId());
            }

            ApiExternaFuncion config = configOpt.get();

            // 3. Validar configuración completa
            if (!apiResolucionService.validarConfiguracionCompleta(config)) {
                return crearRespuestaError(request, "CONFIG_INCOMPLETA", "Configuración del proveedor incompleta", usuarioId, funcion.getApiServicesFuncionId());
            }

            // 4. Construir headers HTTP con token descifrado
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Agregar autorización si está configurada
            if (config.getAutorizacion() != null && !config.getAutorizacion().trim().isEmpty()) {
                String tokenDescifrado = apiResolucionService.descifrarTokenExterno(config.getToken());
                if (tokenDescifrado != null) {
                    headers.set("Authorization", config.getAutorizacion().replace("{TOKEN}", tokenDescifrado));
                } else {
                    headers.set("Authorization", config.getAutorizacion());
                }
            }

            // 5. Construir request según metadata de la función o configuración por defecto
            Map<String, Object> requestBody = construirRequestSms(request, funcion.getRequest(), config.getRequest());

            // 6. Crear entidad HTTP
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 7. Enviar request al proveedor externo
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getEndpoint(),
                    HttpMethod.valueOf(config.getMetodo()),
                    entity,
                    Map.class
            );

            log.info("Respuesta del proveedor SMS: {}", response.getBody());

            // 8. Procesar respuesta exitosa
            SmsResponse smsResponse = SmsResponse.builder()
                    .success(true)
                    .messageId(UUID.randomUUID().toString())
                    .phoneNumber(request.getPhoneNumber())
                    .statusCode(String.valueOf(response.getStatusCode().value()))
                    .statusMessage("SMS enviado correctamente")
                    .timestamp(java.time.LocalDateTime.now())
                    .provider(config.getNombre())
                    .build();

            // 9. Registrar auditoría del consumo exitoso (consulta externa)
            auditoriaService.registrarConsumoExitoso(
                    funcion.getApiServicesFuncionId(), 
                    requestBody, 
                    response.getBody(), 
                    true  // EsConsulta = true para consulta externa SMS
            );

            return smsResponse;

        } catch (Exception e) {
            log.error("Error enviando SMS para usuarioId: {}", usuarioId, e);
            
            // Obtener función para auditoría
            Optional<ApiServicesFuncion> funcionOpt = apiResolucionService.obtenerFuncionInterna(CODIGO_FUNCION_SMS);
            Integer funcionId = funcionOpt.map(ApiServicesFuncion::getApiServicesFuncionId).orElse(null);
            
            // Registrar auditoría del consumo fallido (consulta externa)
            auditoriaService.registrarConsumoFallido(
                    funcionId, 
                    request, 
                    e.getMessage(), 
                    true  // EsConsulta = true para consulta externa SMS
            );

            return SmsResponse.builder()
                    .success(false)
                    .phoneNumber(request.getPhoneNumber())
                    .errorCode("SMS_ERROR")
                    .errorMessage(e.getMessage())
                    .statusCode("500")
                    .statusMessage("Error enviando SMS")
                    .timestamp(java.time.LocalDateTime.now())
                    .provider("Sistema")
                    .build();
        }
    }

    /**
     * Construye el request para SMS usando las metadata de configuración
     */
    private Map<String, Object> construirRequestSms(SmsRequest request, String requestMetadata, String configRequest) {
        // Construir destino
        Map<String, String> destination = new HashMap<>();
        destination.put("to", request.getPhoneNumber());

        // Construir mensaje
        Map<String, Object> message = new HashMap<>();
        message.put("from", request.getSenderId() != null ? request.getSenderId() : "INFOBIT");
        message.put("destinations", List.of(destination));
        message.put("text", request.getMessage());

        // Construir request completo
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("messages", List.of(message));

        return requestBody;
    }

    /**
     * Crea una respuesta de error y registra auditoría
     */
    private SmsResponse crearRespuestaError(SmsRequest request, String errorCode, String errorMessage, 
                                           Integer usuarioId, Integer funcionId) {
        // Registrar auditoría del consumo fallido (consulta externa)
        if (funcionId != null) {
            auditoriaService.registrarConsumoFallido(funcionId, request, errorMessage, true);  // EsConsulta = true para consulta externa SMS
        }

        return SmsResponse.builder()
                .success(false)
                .phoneNumber(request.getPhoneNumber())
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .statusCode("400")
                .statusMessage("Error en envío de SMS")
                .timestamp(java.time.LocalDateTime.now())
                .provider("Sistema")
                .build();
    }

    /**
     * Valida los datos de una solicitud de SMS
     * 
     * @param request Solicitud a validar
     * @return SmsResponse con error de validación o null si es válida
     */
    public SmsResponse validateRequest(SmsRequest request) {

        if (request.getPhoneNumber() == null || request.getPhoneNumber().isEmpty()) {
            return SmsResponse.error("INVALID_PHONE", "Número requerido", request.getPhoneNumber());
        }

        if (request.getMessage() == null || request.getMessage().isEmpty()) {
            return SmsResponse.error("INVALID_MESSAGE", "Mensaje requerido", request.getPhoneNumber());
        }

        if (request.getMessage().length() > 160) {
            return SmsResponse.error("MESSAGE_TOO_LONG", "Máximo 160 caracteres", request.getPhoneNumber());
        }

        return null;
    }

    /**
     * Métodos de compatibilidad con código existente
     */
    public String getApiKey() {
        return "Configuración dinámica desde base de datos";
    }

    public String getApiUrl() {
        return "Configuración dinámica desde base de datos";
    }

    public String getDefaultSender() {
        return "INFOBIT";
    }

    public List<Object> getSmsHistory(String phoneNumber) {
        return new ArrayList<>();
    }

    public List<Object> getSmsHistoryByDateRange(java.time.LocalDateTime inicio,
                                                 java.time.LocalDateTime fin) {
        return new ArrayList<>();
    }
}