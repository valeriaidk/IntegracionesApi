package com.extech.IntegracionesApis.Domain.Dto.Sms;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para respuesta de envío de SMS
 * 
 * Esta clase contiene la respuesta del servidor después de intentar
 * enviar un mensaje SMS a través de la API de Infobip.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta de envío de SMS")
public class SmsResponse {
    
    /**
     * Indica si el SMS fue enviado exitosamente
     * 
     * true: SMS enviado correctamente
     * false: Falló el envío del SMS
     */
    @Schema(description = "Indica si el SMS fue enviado exitosamente", 
            example = "true",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean success;
    
    /**
     * ID único del mensaje generado por el proveedor
     * 
     * Usado para seguimiento del estado del SMS
     * Formato típico: MSG_1715278800000 o UUID del proveedor
     */
    @Schema(description = "ID único del mensaje para seguimiento", 
            example = "MSG_1715278800000",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String messageId;
    
    /**
     * Código de estado HTTP de la respuesta
     * 
     * 200: Envío exitoso
     * 400: Error de cliente
     * 401: Error de autenticación
     * 500: Error del servidor
     */
    @Schema(description = "Código de estado HTTP", 
            example = "200",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String statusCode;
    
    /**
     * Mensaje descriptivo del estado
     * 
     * Proporciona detalles del resultado del envío
     * Útil para diagnóstico y display al usuario
     */
    @Schema(description = "Mensaje descriptivo del estado", 
            example = "SMS enviado correctamente",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String statusMessage;
    
    /**
     * Fecha y hora del procesamiento
     * 
     * Formato ISO 8601: yyyy-MM-ddTHH:mm:ss
     * Zona horaria: UTC
     */
    @Schema(description = "Fecha y hora del procesamiento (UTC)", 
            example = "2026-03-09T10:53:00",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime timestamp;
    
    /**
     * Número de teléfono del destinatario
     * 
     * Incluido para referencia en la respuesta
     * Formato: +CódigoPaísNúmero
     */
    @Schema(description = "Número de teléfono del destinatario", 
            example = "+51987654321",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;
    
    /**
     * Código de error específico (solo si success = false)
     * 
     * Códigos comunes:
     * - INVALID_PHONE: Formato de número inválido
     * - INVALID_MESSAGE: Mensaje inválido
     * - MESSAGE_TOO_LONG: Mensaje excede límite
     * - AUTH_ERROR: Error de autenticación
     * - API_ERROR: Error de la API
     */
    @Schema(description = "Código de error específico (solo si falló el envío)", 
            example = "AUTH_ERROR",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String errorCode;
    
    /**
     * Mensaje de error detallado (solo si success = false)
     * 
     * Proporciona información adicional para diagnóstico
     * Incluye causa raíz del problema
     */
    @Schema(description = "Mensaje de error detallado (solo si falló el envío)", 
            example = "API Key inválida o no autorizada",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String errorMessage;
    
    /**
     * Proveedor del servicio SMS
     * 
     * Actualmente configurado para Infobip
     * Puede extenderse para múltiples proveedores
     */
    @Schema(description = "Proveedor del servicio SMS", 
            example = "Infobip",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String provider;
    
    /**
     * Crea una respuesta de éxito para envío de SMS
     * 
     * @param messageId ID único del mensaje
     * @param phoneNumber Número de teléfono del destinatario
     * @return SmsResponse con success = true
     */
    public static SmsResponse success(String messageId, String phoneNumber) {
        return SmsResponse.builder()
                .success(true)
                .messageId(messageId)
                .phoneNumber(phoneNumber)
                .statusCode("200")
                .statusMessage("SMS enviado exitosamente")
                .timestamp(LocalDateTime.now())
                .provider("Infobip")
                .build();
    }
    
    /**
     * Crea una respuesta de error para envío de SMS
     * 
     * @param errorCode Código de error específico
     * @param errorMessage Mensaje de error detallado
     * @param phoneNumber Número de teléfono del destinatario
     * @return SmsResponse con success = false
     */
    public static SmsResponse error(String errorCode, String errorMessage, String phoneNumber) {
        return SmsResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .phoneNumber(phoneNumber)
                .statusCode("400")
                .statusMessage("Error al enviar SMS")
                .timestamp(LocalDateTime.now())
                .provider("Infobip")
                .build();
    }
}
