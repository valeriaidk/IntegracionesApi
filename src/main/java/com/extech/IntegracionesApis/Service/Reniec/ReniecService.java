package com.extech.IntegracionesApis.Service.Reniec;

import com.extech.IntegracionesApis.Domain.Model.ConfiguracionApiFuncion;
import com.extech.IntegracionesApis.Domain.Model.Log;
import com.extech.IntegracionesApis.Repository.Reniec.ReniecConfiguracionRepository;
import com.extech.IntegracionesApis.Repository.LogRepository;
import com.extech.IntegracionesApis.Domain.Dto.Reniec.ReniecResponse;
import com.extech.IntegracionesApis.Util.Security.TokenEncryptionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReniecService {

    @Autowired
    private ReniecConfiguracionRepository configRepository;

    @Autowired
    private LogRepository logRepository;

    @Autowired
    private RestTemplate restTemplate;

    private ObjectMapper objectMapper = new ObjectMapper();
    private TokenEncryptionUtil encryptionUtil = new TokenEncryptionUtil();

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
            // BUSCAR EN CACHE (IT_Log) - temporalmente desactivado
            // Optional<Log> logCache = logRepository.findByTipoDocumentoAndNumeroDocumento(tipoDocumento, numeroDocumento);
            // if (logCache.isPresent()) {
            //     return objectMapper.readValue(logCache.get().getRespuesta(), ReniecResponse.class);
            // }

            // OBTENER CONFIGURACIÓN ESPECÍFICA DE RENIEC
            ConfiguracionApiFuncion config = configRepository
                .findConfiguracionReniecActiva()
                .orElseThrow(() -> new Exception("No se encontró configuración para: " + tipoDocumento));

            // VERIFICAR QUE TENGA CREDENCIALES
            if (config.getCredencialClave() == null || config.getCredencialClave().isEmpty()) {
                throw new Exception("No se encontró token configurado para: " + tipoDocumento);
            }

            // DESENCRIPTAR TOKEN (de CredencialClave)
            TokenEncryptionUtil crypto = new TokenEncryptionUtil();
            String tokenPlano = crypto.decrypt(config.getCredencialClave());

            // CONSTRUIR URL
            String urlCompleta = config.getUrlEndpoint() + numeroDocumento;

            // LLAMAR API
            ReniecResponse response = llamarApiExterna(urlCompleta, tokenPlano);

            // Agregar metadatos a la respuesta
            response.setTipo(tipoDocumento);
            response.setLimiteConsultas(100); // Límite de consultas por día
            response.setMensaje("Consulta exitosa");
            response.setPlan("free");

            // GUARDAR EN IT_Log (temporalmente comentado)
            // guardarEnLog(tipoDocumento, numeroDocumento, response);

            return response;

        } catch (Exception e) {
            // Registrar error en BD
            // registrarError(tipoDocumento, numeroDocumento, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    // Llamar API externa
    private ReniecResponse llamarApiExterna(String url, String token) {
        try {
            // Configurar headers
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            
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