package com.extech.IntegracionesApis.Domain.Dto.Sms;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para solicitud de envío de SMS
 * 
 * Esta clase contiene los datos necesarios para enviar un mensaje SMS
 * a través de la API de Infobip.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Solicitud de envío de SMS")
public class SmsRequest {
    
    /**
     * Número de teléfono del destinatario
     * 
     * Formato requerido: +CódigoPaísNúmero (ej: +51987654321)
     * - Perú: +51XXXXXXXXX
     * - Internacional: +CódigoPaísNúmero
     */
    @NotBlank(message = "El número de teléfono es obligatorio")
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "El formato del número de teléfono no es válido")
    @Schema(description = "Número de teléfono del destinatario en formato internacional", 
            example = "+51987654321",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String phoneNumber;
    
    /**
     * Contenido del mensaje SMS a enviar
     * 
     * Límite máximo: 160 caracteres
     * No incluye caracteres especiales que puedan causar problemas
     */
    @NotBlank(message = "El mensaje es obligatorio")
    @Size(max = 160, message = "El mensaje no puede exceder los 160 caracteres")
    @Schema(description = "Contenido del mensaje SMS (máximo 160 caracteres)", 
            example = "Hola, este es un mensaje de prueba",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;
    
    /**
     * Identificador del remitente (opcional)
     * 
     * Si no se especifica, se usa el valor por defecto configurado
     * Debe estar previamente registrado en Infobip
     */
    @Schema(description = "ID del remitente (opcional, usa valor por defecto si no se especifica)", 
            example = "INFOBIT",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String senderId;
    
    /**
     * Nombre de la campaña (opcional)
     * 
     * Útil para seguimiento y reportes de campañas
     * No afecta el envío del SMS
     */
    @Schema(description = "Nombre de la campaña para seguimiento (opcional)", 
            example = "Campana_Prueba_2024",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String campaignName;
}
