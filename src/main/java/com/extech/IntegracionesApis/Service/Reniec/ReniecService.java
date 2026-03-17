package com.extech.IntegracionesApis.Service.Reniec;

import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiServicesFuncion;
import com.extech.IntegracionesApis.Service.ApiResolucionService;
import com.extech.IntegracionesApis.Service.AuditoriaService;
import com.extech.IntegracionesApis.Util.Security.UserContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Servicio para consultas RENIEC mediante configuración dinámica de la base de datos
 * 
 * Esta clase maneja la lógica de negocio para consultar datos de DNI/RUC a RENIEC,
 * utilizando la configuración resuelta desde la base de datos a través
 * del flujo: TokenUsuario -> Usuario -> Función -> SP -> Configuración Externa
 * 
 * @author Extech
 * @version 2.0
 * @since 2026-03-16
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReniecService {

    private final RestTemplate restTemplate;
    private final ApiResolucionService apiResolucionService;
    private final AuditoriaService auditoriaService;

    /**
     * Códigos de función interna para RENIEC
     */
    private static final String CODIGO_FUNCION_RENIEC_DNI = "RENIEC_DNI";
    private static final String CODIGO_FUNCION_RENIEC_RUC = "RENIEC_RUC";

    /**
     * Consulta DNI a RENIEC usando configuración resuelta desde base de datos
     * 
     * @param numeroDni Número de DNI a consultar
     * @return JSON con los datos del DNI consultado
     * @throws Exception Si hay error en la consulta
     */
    public String consultarDNI(String numeroDni) throws Exception {
        return consultarReniec(numeroDni, CODIGO_FUNCION_RENIEC_DNI, "DNI");
    }

    /**
     * Consulta RUC a RENIEC usando configuración resuelta desde base de datos
     * 
     * @param numeroRuc Número de RUC a consultar
     * @return JSON con los datos del RUC consultado
     * @throws Exception Si hay error en la consulta
     */
    public String consultarRUC(String numeroRuc) throws Exception {
        return consultarReniec(numeroRuc, CODIGO_FUNCION_RENIEC_RUC, "RUC");
    }

    /**
     * Método principal para consultas a RENIEC
     * 
     * @param numeroDocumento Número de documento a consultar
     * @param codigoFuncion Código de la función interna
     * @param tipoDocumento Tipo de documento (DNI/RUC)
     * @return JSON con los datos consultados
     * @throws Exception Si hay error en la consulta
     */
    private String consultarReniec(String numeroDocumento, String codigoFuncion, String tipoDocumento) throws Exception {
        
        Integer usuarioId = UserContext.getUsuarioId();
        if (usuarioId == null) {
            throw new Exception("Usuario no autenticado");
        }

        log.info("Consultando {} para usuarioId: {} - {}", tipoDocumento, usuarioId, numeroDocumento);

        try {
            // 1. Obtener función interna
            Optional<ApiServicesFuncion> funcionOpt = apiResolucionService.obtenerFuncionInterna(codigoFuncion);
            if (funcionOpt.isEmpty()) {
                throw new Exception("Función " + codigoFuncion + " no configurada");
            }

            ApiServicesFuncion funcion = funcionOpt.get();

            // 2. Resolver configuración externa usando el SP
            Optional<ApiExternaFuncion> configOpt = apiResolucionService.resolverConfiguracionExterna(usuarioId, codigoFuncion);
            if (configOpt.isEmpty()) {
                throw new Exception("Configuración de proveedor RENIEC no encontrada para usuario: " + usuarioId);
            }

            ApiExternaFuncion config = configOpt.get();

            // 3. Validar configuración completa
            if (!apiResolucionService.validarConfiguracionCompleta(config)) {
                throw new Exception("Configuración del proveedor RENIEC incompleta");
            }

            // 4. Construir headers HTTP con token descifrado
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Agregar autorización si está configurada
            if (config.getAutorizacion() != null && !config.getAutorizacion().trim().isEmpty()) {
                String tokenDescifrado = apiResolucionService.descifrarTokenExterno(config.getToken());
                if (tokenDescifrado != null) {
                    headers.set("Authorization", config.getAutorizacion().replace("{TOKEN}", tokenDescifrado));
                } else {
                    headers.set("Authorization", config.getAutorizacion());
                }
            }

            // 5. Construir request según metadata de la función
            Map<String, Object> requestBody = construirRequestReniec(numeroDocumento, tipoDocumento, funcion.getRequest(), config.getRequest());

            // 6. Crear entidad HTTP
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 7. Enviar request al proveedor externo
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getEndpoint(),
                    HttpMethod.valueOf(config.getMetodo()),
                    entity,
                    Map.class
            );

            log.info("Respuesta de RENIEC para {}: {}", tipoDocumento, response.getBody());

            // 8. Procesar respuesta
            String respuestaJson = response.getBody() != null ? response.getBody().toString() : "{}";

            // 9. Registrar auditoría del consumo exitoso
            auditoriaService.registrarConsumoExitoso(
                    funcion.getApiServicesFuncionId(), 
                    requestBody, 
                    response.getBody(), 
                    true
            );

            return respuestaJson;

        } catch (Exception e) {
            log.error("Error consultando {} para usuarioId: {}", tipoDocumento, usuarioId, e);
            
            // Obtener función para auditoría
            Optional<ApiServicesFuncion> funcionOpt = apiResolucionService.obtenerFuncionInterna(codigoFuncion);
            Integer funcionId = funcionOpt.map(ApiServicesFuncion::getApiServicesFuncionId).orElse(null);
            
            // Registrar auditoría del consumo fallido
            if (funcionId != null) {
                auditoriaService.registrarConsumoFallido(
                        funcionId, 
                        Map.of("tipo", tipoDocumento, "numero", numeroDocumento), 
                        e.getMessage(), 
                        true
                );
            }

            throw new Exception("Error consultando " + tipoDocumento + ": " + e.getMessage());
        }
    }

    /**
     * Construye el request para RENIEC usando las metadata de configuración
     */
    private Map<String, Object> construirRequestReniec(String numeroDocumento, String tipoDocumento, 
                                                      String requestMetadata, String configRequest) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // Construir request básico según el tipo de documento
        if ("DNI".equals(tipoDocumento)) {
            requestBody.put("dni", numeroDocumento);
            requestBody.put("tipo", "dni");
        } else if ("RUC".equals(tipoDocumento)) {
            requestBody.put("ruc", numeroDocumento);
            requestBody.put("tipo", "ruc");
        }

        // Aquí se podría usar requestMetadata o configRequest para personalizar el request
        // si el proveedor externo requiere un formato específico
        
        return requestBody;
    }
}
