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
            // 1. Resolver configuración externa usando el SP (basado en usuario + código de función)
            Optional<ApiExternaFuncion> configOpt = apiResolucionService.resolverConfiguracionExterna(usuarioId, codigoFuncion);
            if (configOpt.isEmpty()) {
                throw new Exception("Configuración de proveedor RENIEC no encontrada para usuario: " + usuarioId);
            }

            ApiExternaFuncion config = configOpt.get();

            // Función interna (opcional) solo para auditoría
            Integer apiServicesFuncionId = apiResolucionService
                    .obtenerFuncionInterna(codigoFuncion)
                    .map(ApiServicesFuncion::getApiServicesFuncionId)
                    .orElse(null);

            // 2. Validar configuración completa
            if (!apiResolucionService.validarConfiguracionCompleta(config)) {
                throw new Exception("Configuración del proveedor RENIEC incompleta");
            }

            // 3. Construir headers HTTP con token descifrado
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            // Agregar autorización si está configurada
            if (config.getAutorizacion() != null && !config.getAutorizacion().trim().isEmpty()) {
                String tokenDescifrado = apiResolucionService.descifrarTokenExterno(config.getToken());
                String authCfg = config.getAutorizacion().trim();
                if (tokenDescifrado != null && !tokenDescifrado.isEmpty()) {
                    if (authCfg.contains("{TOKEN}")) {
                        headers.set("Authorization", authCfg.replace("{TOKEN}", tokenDescifrado));
                    } else if ("BEARER".equalsIgnoreCase(authCfg)) {
                        headers.set("Authorization", "Bearer " + tokenDescifrado);
                    } else {
                        // Si solo viene un prefijo u otro formato, concatenar token al final
                        headers.set("Authorization", authCfg + " " + tokenDescifrado);
                    }
                } else {
                    headers.set("Authorization", authCfg);
                }
            }

            // 4. Construir request (por ahora básico; config.request puede usarse para personalizar)
            Map<String, Object> requestBody = construirRequestReniec(numeroDocumento, tipoDocumento, null, config.getRequest());

            // 5. Crear entidad HTTP
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 6. Enviar request al proveedor externo
            String url = config.getEndpoint();
            HttpMethod method = HttpMethod.valueOf(config.getMetodo());
            if (method == HttpMethod.GET && url != null) {
                // Proveedores tipo DeColecta suelen exponer ...?numero= y esperan concatenación del valor
                if (url.endsWith("=")) {
                    url = url + numeroDocumento;
                }
            }

            ResponseEntity<Map> response = restTemplate.exchange(
                    url,
                    method,
                    entity,
                    Map.class
            );

            log.info("Respuesta de RENIEC para {}: {}", tipoDocumento, response.getBody());

            // 7. Procesar respuesta
            String respuestaJson = response.getBody() != null ? response.getBody().toString() : "{}";

            // 8. Auditoría (si no hay ApiServicesFuncionId, se registra sin función)
            auditoriaService.registrarConsumoExitoso(
                    apiServicesFuncionId,
                    requestBody,
                    response.getBody(),
                    true
            );

            return respuestaJson;

        } catch (Exception e) {
            log.error("Error consultando {} para usuarioId: {}", tipoDocumento, usuarioId, e);

            // Registrar auditoría del consumo fallido sin función interna
            auditoriaService.registrarConsumoFallido(
                    apiResolucionService.obtenerFuncionInterna(codigoFuncion)
                            .map(ApiServicesFuncion::getApiServicesFuncionId)
                            .orElse(null),
                    Map.of("tipo", tipoDocumento, "numero", numeroDocumento),
                    e.getMessage(),
                    true
            );

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
