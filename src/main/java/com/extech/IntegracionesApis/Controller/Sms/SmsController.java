package com.extech.IntegracionesApis.Controller.Sms;

import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsRequest;
import com.extech.IntegracionesApis.Domain.Dto.Sms.SmsResponse;
import com.extech.IntegracionesApis.Repository.Sms.SmsRepository;
import com.extech.IntegracionesApis.Service.Sms.SmsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
@Slf4j
public class SmsController {
    
    private final SmsService smsService;
    private final SmsRepository smsRepository;
    
    @PostMapping("/send")
    public ResponseEntity<SmsResponse> sendSms(
            @Valid @RequestBody SmsRequest request) {
        
        log.info("Recibida solicitud para enviar SMS al número: {}", request.getPhoneNumber());
        
        SmsResponse validationResponse = smsService.validateRequest(request);
        if (validationResponse != null) {
            return ResponseEntity.badRequest().body(validationResponse);
        }
        
        SmsResponse response = smsService.sendSms(request);
        
        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    
    @PostMapping("/send-batch")
    public ResponseEntity<?> sendBatchSms(
            @Valid @RequestBody java.util.List<SmsRequest> requests) {
        
        log.info("Recibida solicitud para enviar {} SMS", requests.size());
        
        if (requests.size() > 100) {
            return ResponseEntity.badRequest()
                .body("No se pueden enviar más de 100 SMS en una sola solicitud");
        }
        
        java.util.List<SmsResponse> responses = new java.util.ArrayList<>();
        
        for (SmsRequest request : requests) {
            SmsResponse validationResponse = smsService.validateRequest(request);
            if (validationResponse != null) {
                responses.add(validationResponse);
                continue;
            }
            
            responses.add(smsService.sendSms(request));
        }
        
        return ResponseEntity.ok(responses);
    }
    
    @GetMapping("/status/{messageId}")
    public ResponseEntity<SmsResponse> checkStatus(
            @PathVariable String messageId) {
        
        log.info("Verificando estado del mensaje: {}", messageId);
        
        SmsResponse response = SmsResponse.builder()
                .success(true)
                .messageId(messageId)
                .statusCode("DELIVERED")
                .statusMessage("SMS entregado exitosamente")
                .timestamp(java.time.LocalDateTime.now())
                .provider("Infobit")
                .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/history/{phoneNumber}")
    public ResponseEntity<?> getSmsHistory(
            @PathVariable String phoneNumber) {
        
        log.info("Consultando historial de SMS para el teléfono: {}", phoneNumber);
        
        java.util.List<com.extech.IntegracionesApis.Domain.Model.Sms> history = smsService.getSmsHistory(phoneNumber);
        
        if (history.isEmpty()) {
            return ResponseEntity.ok("No se encontraron registros para el número: " + phoneNumber);
        }
        
        return ResponseEntity.ok(history);
    }
    
    @GetMapping("/history")
    public ResponseEntity<?> getSmsHistoryByDateRange(
            @RequestParam String fechaInicio,
            @RequestParam String fechaFin) {
        
        try {
            java.time.LocalDateTime inicio = java.time.LocalDateTime.parse(fechaInicio);
            java.time.LocalDateTime fin = java.time.LocalDateTime.parse(fechaFin);
            
            log.info("Consultando historial de SMS entre {} y {}", inicio, fin);
            
            java.util.List<com.extech.IntegracionesApis.Domain.Model.Sms> history = 
                smsService.getSmsHistoryByDateRange(inicio, fin);
            
            if (history.isEmpty()) {
                return ResponseEntity.ok("No se encontraron registros en el rango de fechas especificado");
            }
            
            return ResponseEntity.ok(history);
            
        } catch (Exception e) {
            log.error("Error al parsear fechas: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("Formato de fecha inválido. Use: yyyy-MM-ddTHH:mm:ss");
        }
    }
    
    @GetMapping("/statistics")
    public ResponseEntity<?> getStatistics(
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        
        try {
            java.time.LocalDateTime inicio = fechaInicio != null ? 
                java.time.LocalDateTime.parse(fechaInicio) : 
                java.time.LocalDateTime.now().minusDays(30);
            
            java.time.LocalDateTime fin = fechaFin != null ? 
                java.time.LocalDateTime.parse(fechaFin) : 
                java.time.LocalDateTime.now();
            
            Long successfulSms = smsRepository.countSuccessfulSmsBetween(inicio, fin);
            Long failedSms = smsRepository.countFailedSmsBetween(inicio, fin);
            java.util.List<Object[]> smsByProvider = smsRepository.countSmsByProviderBetween(inicio, fin);
            
            java.util.Map<String, Object> statistics = new java.util.HashMap<>();
            statistics.put("periodo", java.util.Map.of("inicio", inicio, "fin", fin));
            statistics.put("exitosos", successfulSms);
            statistics.put("fallidos", failedSms);
            statistics.put("total", successfulSms + failedSms);
            statistics.put("tasaExito", successfulSms > 0 ? 
                (successfulSms.doubleValue() / (successfulSms + failedSms)) * 100 : 0.0);
            
            java.util.Map<String, Long> byProvider = new java.util.HashMap<>();
            for (Object[] result : smsByProvider) {
                byProvider.put((String) result[0], (Long) result[1]);
            }
            statistics.put("porProveedor", byProvider);
            
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            log.error("Error al generar estadísticas: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body("Error al generar estadísticas: " + e.getMessage());
        }
    }
}
