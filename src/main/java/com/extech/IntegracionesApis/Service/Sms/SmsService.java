package com.extech.IntegracionesApis.Service.Sms;

import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsRequest;
import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsResponse;
import com.extech.IntegracionesApis.Domain.Model.Sms;
import com.extech.IntegracionesApis.Repository.Sms.SmsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsService {
    
    private final RestTemplate restTemplate;
    private final SmsRepository smsRepository;
    
    @Value("${infobit.api.url}")
    private String infobitApiUrl;
    
    @Value("${infobit.api.key}")
    private String infobitApiKey;
    
    @Value("${infobit.api.sender:INFOBIT}")
    private String defaultSenderId;
    
    @Transactional
    public SmsResponse sendSms(SmsRequest request) {
        Sms smsEntity = createSmsEntity(request);
        
        try {
            log.info("Enviando SMS al número: {}", request.getPhoneNumber());
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "App " + infobitApiKey);
            
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("from", request.getSenderId() != null ? request.getSenderId() : defaultSenderId);
            body.add("to", request.getPhoneNumber());
            body.add("text", request.getMessage());
            
            if (request.getCampaignName() != null) {
                body.add("campaign", request.getCampaignName());
            }
            
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                infobitApiUrl,
                HttpMethod.POST,
                entity,
                String.class
            );
            
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("SMS enviado exitosamente. Response: {}", response.getBody());
                
                String messageId = extractMessageId(response.getBody());
                smsEntity.setMessageId(messageId);
                smsEntity.setSuccess(true);
                smsEntity.setStatusCode("200");
                smsEntity.setStatusMessage("SMS enviado exitosamente");
                
                SmsResponse smsResponse = SmsResponse.success(messageId, request.getPhoneNumber());
                smsEntity.setErrorMessage(null);
                smsEntity.setErrorCode(null);
                
                smsRepository.save(smsEntity);
                return smsResponse;
            } else {
                log.error("Error al enviar SMS. Status: {}, Body: {}", response.getStatusCode(), response.getBody());
                
                smsEntity.setSuccess(false);
                smsEntity.setStatusCode(String.valueOf(response.getStatusCode().value()));
                smsEntity.setStatusMessage("Error en la respuesta del servidor");
                smsEntity.setErrorCode("HTTP_ERROR");
                smsEntity.setErrorMessage("Error en la respuesta del servidor: " + response.getStatusCode());
                
                smsRepository.save(smsEntity);
                return SmsResponse.error("HTTP_ERROR", "Error en la respuesta del servidor", request.getPhoneNumber());
            }
            
        } catch (Exception e) {
            log.error("Error al enviar SMS al número {}: {}", request.getPhoneNumber(), e.getMessage(), e);
            
            smsEntity.setSuccess(false);
            smsEntity.setStatusCode("500");
            smsEntity.setStatusMessage("Error interno del servidor");
            smsEntity.setErrorCode("EXCEPTION");
            smsEntity.setErrorMessage(e.getMessage());
            
            smsRepository.save(smsEntity);
            return SmsResponse.error("EXCEPTION", e.getMessage(), request.getPhoneNumber());
        }
    }
    
    private Sms createSmsEntity(SmsRequest request) {
        Sms sms = new Sms();
        sms.setPhoneNumber(request.getPhoneNumber());
        sms.setMessage(request.getMessage());
        sms.setSenderId(request.getSenderId() != null ? request.getSenderId() : defaultSenderId);
        sms.setCampaignName(request.getCampaignName());
        sms.setProvider("Infobit");
        sms.setSuccess(false);
        
        return sms;
    }
    
    private String extractMessageId(String responseBody) {
        try {
            if (responseBody.contains("\"message_id\"")) {
                int start = responseBody.indexOf("\"message_id\":\"") + 14;
                int end = responseBody.indexOf("\"", start);
                return responseBody.substring(start, end);
            }
            return "MSG_" + System.currentTimeMillis();
        } catch (Exception e) {
            log.warn("No se pudo extraer message_id del response: {}", responseBody);
            return "MSG_" + System.currentTimeMillis();
        }
    }
    
    public SmsResponse validateRequest(SmsRequest request) {
        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            return SmsResponse.error("INVALID_PHONE", "El número de teléfono es requerido", request.getPhoneNumber());
        }
        
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            return SmsResponse.error("INVALID_MESSAGE", "El mensaje es requerido", request.getPhoneNumber());
        }
        
        if (request.getMessage().length() > 160) {
            return SmsResponse.error("MESSAGE_TOO_LONG", "El mensaje excede los 160 caracteres", request.getPhoneNumber());
        }
        
        return null;
    }
    
    @Transactional(readOnly = true)
    public java.util.List<Sms> getSmsHistory(String phoneNumber) {
        return smsRepository.findByPhoneNumber(phoneNumber);
    }
    
    @Transactional(readOnly = true)
    public java.util.List<Sms> getSmsHistoryByDateRange(java.time.LocalDateTime fechaInicio, java.time.LocalDateTime fechaFin) {
        return smsRepository.findByFechaEnvioBetween(fechaInicio, fechaFin);
    }
}
