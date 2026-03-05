package com.extech.IntegracionesApis.Domain.Dto.Sms;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmsResponse {
    
    private boolean success;
    
    private String messageId;
    
    private String statusCode;
    
    private String statusMessage;
    
    private LocalDateTime timestamp;
    
    private String phoneNumber;
    
    private String errorCode;
    
    private String errorMessage;
    
    private String provider;
    
    public static SmsResponse success(String messageId, String phoneNumber) {
        return SmsResponse.builder()
                .success(true)
                .messageId(messageId)
                .phoneNumber(phoneNumber)
                .statusCode("200")
                .statusMessage("SMS enviado exitosamente")
                .timestamp(LocalDateTime.now())
                .provider("Infobit")
                .build();
    }
    
    public static SmsResponse error(String errorCode, String errorMessage, String phoneNumber) {
        return SmsResponse.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .phoneNumber(phoneNumber)
                .statusCode("400")
                .statusMessage("Error al enviar SMS")
                .timestamp(LocalDateTime.now())
                .provider("Infobit")
                .build();
    }
}
