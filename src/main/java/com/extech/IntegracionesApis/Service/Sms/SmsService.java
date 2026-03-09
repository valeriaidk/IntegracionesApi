package com.extech.IntegracionesApis.Service.Sms;

import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsRequest;
import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Servicio para el envío de SMS mediante la API de Infobip
 * 
 * Esta clase maneja la lógica de negocio para enviar mensajes SMS,
 * incluyendo validación, construcción de requests y manejo de errores.
 * 
 * @author Extech
 * @version 1.0
 * @since 2026-03-09
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {

    private final RestTemplate restTemplate;

    /**
     * URL de la API de Infobip para envío de SMS
     */
    @Value("${infobip.api.url}")
    private String apiUrl;

    /**
     * API Key para autenticación con Infobip
     */
    @Value("${infobip.api.key}")
    private String apiKey;

    /**
     * ID del remitente por defecto configurado
     */
    @Value("${infobip.api.sender}")
    private String sender;

    /**
     * Envía un mensaje SMS a través de la API de Infobip
     * 
     * Este método construye el request HTTP, lo envía a Infobip
     * y procesa la respuesta.
     * 
     * @param request Objeto con los datos del SMS a enviar
     * @return SmsResponse con el resultado del envío
     */
    public SmsResponse sendSms(SmsRequest request) {

        try {

            log.info("Enviando SMS a {}", request.getPhoneNumber());

            // Construir headers HTTP
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "App " + apiKey);

            // Construir destino
            Map<String, String> destination = new HashMap<>();
            destination.put("to", request.getPhoneNumber());

            // Construir mensaje según formato de Infobip
            Map<String, Object> message = new HashMap<>();
            message.put("from",
                    request.getSenderId() != null ?
                            request.getSenderId() : sender);

            message.put("destinations", List.of(destination));
            message.put("text", request.getMessage());

            // Construir request completo
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", List.of(message));

            // Crear entidad HTTP
            HttpEntity<Map<String, Object>> entity =
                    new HttpEntity<>(requestBody, headers);

            // Enviar request a Infobip
            ResponseEntity<Map> response =
                    restTemplate.exchange(
                            apiUrl,
                            HttpMethod.POST,
                            entity,
                            Map.class
                    );

            log.info("Respuesta de Infobip: {}", response.getBody());

            // Procesar respuesta exitosa
            String messageId = UUID.randomUUID().toString();

            return SmsResponse.builder()
                    .success(true)
                    .messageId(messageId)
                    .phoneNumber(request.getPhoneNumber())
                    .statusCode("200")
                    .statusMessage("SMS enviado correctamente")
                    .timestamp(java.time.LocalDateTime.now())
                    .provider("Infobip")
                    .build();

        } catch (Exception e) {

            log.error("Error enviando SMS", e);

            return SmsResponse.builder()
                    .success(false)
                    .phoneNumber(request.getPhoneNumber())
                    .errorCode("SMS_ERROR")
                    .errorMessage(e.getMessage())
                    .statusCode("500")
                    .statusMessage("Error enviando SMS")
                    .timestamp(java.time.LocalDateTime.now())
                    .provider("Infobip")
                    .build();

        }
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
     * Obtiene la API Key configurada
     * 
     * @return API Key de Infobip
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Obtiene la URL de la API configurada
     * 
     * @return URL de la API de Infobip
     */
    public String getApiUrl() {
        return apiUrl;
    }

    /**
     * Obtiene el sender ID por defecto configurado
     * 
     * @return ID del remitente por defecto
     */
    public String getDefaultSender() {
        return sender;
    }

    /**
     * Obtiene el historial de SMS por número de teléfono
     * 
     * @param phoneNumber Número de teléfono a consultar
     * @return Lista de SMS enviados (simulado)
     */
    public List<Object> getSmsHistory(String phoneNumber) {
        return new ArrayList<>();
    }

    /**
     * Obtiene el historial de SMS por rango de fechas
     * 
     * @param inicio Fecha de inicio del rango
     * @param fin Fecha de fin del rango
     * @return Lista de SMS enviados (simulado)
     */
    public List<Object> getSmsHistoryByDateRange(java.time.LocalDateTime inicio,
                                                 java.time.LocalDateTime fin) {
        return new ArrayList<>();
    }
}