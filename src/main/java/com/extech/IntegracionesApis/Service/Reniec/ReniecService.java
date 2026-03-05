package com.extech.IntegracionesApis.Service.Reniec;

import com.extech.IntegracionesApis.Domain.Model.ConsultaApi;
import com.extech.IntegracionesApis.Domain.Model.Log;
import com.extech.IntegracionesApis.Repository.Reniec.ConsultaApiRepository;
import com.extech.IntegracionesApis.Repository.LogRepository;
import com.extech.IntegracionesApis.Domain.Dto.Reniec.ReniecResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReniecService {

    @Autowired
    private ConsultaApiRepository consultaApiRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();

    // Consultar DNI
    public ReniecResponse consultarDNI(String numeroDocumento) {
        return consultarReniecSunat("DNI", numeroDocumento);
    }

    // Consultar RUC
    public ReniecResponse consultarRUC(String numeroDocumento) {
        return consultarReniecSunat("RUC", numeroDocumento);
    }

    // METODO PRINCIPAL
    private ReniecResponse consultarReniecSunat(String tipoDocumento, String numeroDocumento) {
        try {
            // BUSCAR EN CACHE (IT_Log)
            Optional<Log> logCache = logRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento);
            if (logCache.isPresent()) {
                return objectMapper.readValue(logCache.get().getRespuesta(), ReniecResponse.class);
            }

            // OBTENER CONFIGURACIÓN DEL SP (uspConsultaSunatReniec)
            ConsultaApi consultaConfig = consultaApiRepository.findByDocumentoAndActivo(tipoDocumento, true);
            if (consultaConfig == null) {
                throw new Exception("No se encontró configuración para: " + tipoDocumento);
            }

            // CONSTRUIR URL COMPLETA
            String urlCompleta = consultaConfig.getBaseUrl() + consultaConfig.getEndpoint() + numeroDocumento;

            // LLAMAR A LA API EXTERNA
            ReniecResponse response = llamarApiExterna(urlCompleta, consultaConfig);

            // GUARDAR EN IT_LOG
            guardarEnLog(tipoDocumento, numeroDocumento, response);

            return response;

        } catch (Exception e) {
            // Registrar error en BD
            registrarError(tipoDocumento, numeroDocumento, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Llamar API externa
    private ReniecResponse llamarApiExterna(String url, ConsultaApi config) {
        try {
            // Configurar headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + config.getToken());
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);

            // Hacer request GET
            org.springframework.http.ResponseEntity<ReniecResponse> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                ReniecResponse.class
            );

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Error al consultar API: " + e.getMessage(), e);
        }
    }

    // Guardar en IT_Log
    private void guardarEnLog(String tipoDocumento, String numeroDocumento, ReniecResponse response) {
        try {
            Log log = new Log();
            log.setTipoDocumento(tipoDocumento);
            log.setNumeroDocumento(numeroDocumento);
            log.setHttpStatus(200);
            log.setRespuesta(objectMapper.writeValueAsString(response));
            log.setFechaRegistro(LocalDateTime.now());
            log.setActivo(true);
            
            logRepository.save(log);
        } catch (Exception e) {
            System.err.println("Error al guardar en log: " + e.getMessage());
        }
    }

    //Registrar errores
    private void registrarError(String tipoDocumento, String numeroDocumento, String mensaje) {
        try {
            Log log = new Log();
            log.setTipoDocumento(tipoDocumento);
            log.setNumeroDocumento(numeroDocumento);
            log.setHttpStatus(500);
            log.setMensaje(mensaje);
            log.setFechaRegistro(LocalDateTime.now());
            log.setActivo(false);
            
            logRepository.save(log);
        } catch (Exception e) {
            System.err.println("Error al registrar error: " + e.getMessage());
        }
    }
}