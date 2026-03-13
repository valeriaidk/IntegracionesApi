package com.extech.IntegracionesApis.Service.Sunat;

import com.extech.IntegracionesApis.Domain.Model.ConfiguracionApiFuncion;
import com.extech.IntegracionesApis.Repository.Sunat.SunatConfiguracionRepository;
import com.extech.IntegracionesApis.Domain.Dto.Sunat.SunatResponse;
import com.extech.IntegracionesApis.Repository.Sunat.SunatLogRepository;
import com.extech.IntegracionesApis.Util.Security.TokenEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;

@Service
public class SunatService {

    @Autowired
    private SunatConfiguracionRepository configRepository;

    @Autowired
    private SunatLogRepository sunatLogRepository;

    @Autowired
    private RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private TokenEncryptionUtil encryptionUtil = new TokenEncryptionUtil();

    // MÉTODO PRINCIPAL: Consultar RUC
    public SunatResponse consultarRUC(String numeroDocumento) {
        try {
            // BUSCAR EN CACHE (IT_Log) - temporalmente desactivado
            // Optional<Log> logCache = sunatLogRepository.findByTipoDocumentoAndNumeroDocumento("RUC", numeroDocumento);
            // if (logCache.isPresent()) {
            //     return objectMapper.readValue(logCache.get().getRespuesta(), SunatResponse.class);
            // }

            // OBTENER CONFIGURACIÓN ESPECÍFICA DE SUNAT
            ConfiguracionApiFuncion config = configRepository
                .findConfiguracionSunatActiva()
                .orElseThrow(() -> new Exception("No se encontró configuración para RUC"));

            // VERIFICAR QUE TENGA CREDENCIALES
            if (config.getCredencialClave() == null || config.getCredencialClave().isEmpty()) {
                throw new Exception("No se encontró token configurado para RUC");
            }

            // DESENCRIPTAR TOKEN (de CredencialClave)
            TokenEncryptionUtil crypto = new TokenEncryptionUtil();
            String tokenPlano = crypto.decrypt(config.getCredencialClave());

            // CONSTRUIR URL
            String urlCompleta = config.getUrlEndpoint() + numeroDocumento;

            // LLAMAR API
            SunatResponse response = llamarApiSunat(urlCompleta, tokenPlano);

            // Agregar metadatos a la respuesta
            response.setTipoConsulta("RUC");
            response.setLimiteConsultas(100); // Límite de consultas por día
            response.setMensaje("Consulta exitosa");
            response.setPlan("free");

            // GUARDAR EN IT_Log (temporalmente comentado)
            // guardarEnLog("RUC", numeroDocumento, response, 200);

            return response;

        } catch (Exception e) {
            // registrarError("RUC", numeroDocumento, e.getMessage());
            throw new RuntimeException("Error al consultar RUC: " + e.getMessage(), e);
        }
    }

    // Llamar API SUNAT
    private SunatResponse llamarApiSunat(String url, String token) {
        try {
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
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