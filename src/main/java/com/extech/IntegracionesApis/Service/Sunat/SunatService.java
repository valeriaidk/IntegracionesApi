package com.extech.IntegracionesApis.Service.Sunat;

import com.extech.IntegracionesApis.Domain.Model.ConsultaApi;
import com.extech.IntegracionesApis.Domain.Model.Log;
import com.extech.IntegracionesApis.Domain.Dto.Sunat.SunatResponse;
import com.extech.IntegracionesApis.Repository.Reniec.ConsultaApiRepository;
import com.extech.IntegracionesApis.Repository.Sunat.SunatLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class SunatService {

    @Autowired
    private ConsultaApiRepository consultaApiRepository;

    @Autowired
    private SunatLogRepository sunatLogRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // MÉTODO PRINCIPAL: Consultar RUC
    public SunatResponse consultarRUC(String numeroDocumento) {
        try {
            // BUSCAR EN CACHE (IT_Log)
            Optional<Log> logCache = sunatLogRepository.findByTipoDocumentoAndNumeroDocumento("RUC", numeroDocumento);
            if (logCache.isPresent()) {
                return objectMapper.readValue(logCache.get().getRespuesta(), SunatResponse.class);
            }

            // OBTENER CONFIGURACIÓN (uspConsultaSunatReniec)
            ConsultaApi consultaConfig = consultaApiRepository.findByDocumentoAndActivo("RUC", true);
            if (consultaConfig == null) {
                throw new Exception("No se encontró configuración para RUC en IT_Consultas");
            }

            // CONSTRUIR URL COMPLETA
            String urlCompleta = consultaConfig.getBaseUrl() + consultaConfig.getEndpoint() + numeroDocumento;

            // LLAMAR A LA API EXTERNA
            SunatResponse response = llamarApiSunat(urlCompleta, consultaConfig);

            // GUARDAR EN IT_Log
            guardarEnLog("RUC", numeroDocumento, response, 200);

            return response;

        } catch (Exception e) {
            registrarError("RUC", numeroDocumento, e.getMessage());
            throw new RuntimeException("Error al consultar RUC: " + e.getMessage(), e);
        }
    }

    // Llamar API SUNAT
    private SunatResponse llamarApiSunat(String url, ConsultaApi config) {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + config.getToken());
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<SunatResponse> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                SunatResponse.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                throw new Exception("Error HTTP: " + response.getStatusCode());
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al llamar API SUNAT: " + e.getMessage(), e);
        }
    }

    // Guardar en IT_Log
    private void guardarEnLog(String tipoDocumento, String numeroDocumento, SunatResponse response, int httpStatus) {
        try {
            Log log = new Log();
            log.setTipoDocumento(tipoDocumento);
            log.setNumeroDocumento(numeroDocumento);
            log.setHttpStatus(httpStatus);
            log.setRespuesta(objectMapper.writeValueAsString(response));
            log.setFechaRegistro(LocalDateTime.now());
            log.setActivo(true);

            sunatLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Error al guardar en log: " + e.getMessage());
        }
    }

    // Registrar errores
    private void registrarError(String tipoDocumento, String numeroDocumento, String mensaje) {
        try {
            Log log = new Log();
            log.setTipoDocumento(tipoDocumento);
            log.setNumeroDocumento(numeroDocumento);
            log.setHttpStatus(500);
            log.setMensaje(mensaje);
            log.setFechaRegistro(LocalDateTime.now());
            log.setActivo(false);

            sunatLogRepository.save(log);
        } catch (Exception e) {
            System.err.println("Error al registrar error: " + e.getMessage());
        }
    }
}