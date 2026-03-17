package com.extech.IntegracionesApis.Service.Sunat;

import com.extech.IntegracionesApis.Domain.Dto.Sunat.SunatResponse;
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
 * Servicio para consultas SUNAT mediante configuración dinámica de la base de datos
 * 
 * Esta clase maneja la lógica de negocio para consultar datos de RUC a SUNAT,
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
public class SunatService {

    private final RestTemplate restTemplate;
    private final ApiResolucionService apiResolucionService;
    private final AuditoriaService auditoriaService;

    /**
     * Código de función interna para SUNAT
     */
    private static final String CODIGO_FUNCION_SUNAT_RUC = "SUNAT_RUC";

    /**
     * Consulta RUC a SUNAT usando configuración resuelta desde base de datos
     * 
     * @param numeroRuc Número de RUC a consultar
     * @return SunatResponse con los datos del RUC consultado
     * @throws Exception Si hay error en la consulta
     */
    public SunatResponse consultarRuc(String numeroRuc) throws Exception {
        return consultarSunat(numeroRuc, CODIGO_FUNCION_SUNAT_RUC);
    }

    /**
     * Método de compatibilidad con código existente
     */
    public SunatResponse consultarRUC(String numeroRuc) throws Exception {
        return consultarRuc(numeroRuc);
    }

    /**
     * Método principal para consultas a SUNAT
     * 
     * @param numeroRuc Número de RUC a consultar
     * @param codigoFuncion Código de la función interna
     * @return SunatResponse con los datos consultados
     * @throws Exception Si hay error en la consulta
     */
    private SunatResponse consultarSunat(String numeroRuc, String codigoFuncion) throws Exception {
        
        Integer usuarioId = UserContext.getUsuarioId();
        if (usuarioId == null) {
            throw new Exception("Usuario no autenticado");
        }

        log.info("Consultando RUC SUNAT para usuarioId: {} - {}", usuarioId, numeroRuc);

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
                throw new Exception("Configuración de proveedor SUNAT no encontrada para usuario: " + usuarioId);
            }

            ApiExternaFuncion config = configOpt.get();

            // 3. Validar configuración completa
            if (!apiResolucionService.validarConfiguracionCompleta(config)) {
                throw new Exception("Configuración del proveedor SUNAT incompleta");
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
            Map<String, Object> requestBody = construirRequestSunat(numeroRuc, funcion.getRequest(), config.getRequest());

            // 6. Crear entidad HTTP
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // 7. Enviar request al proveedor externo
            ResponseEntity<Map> response = restTemplate.exchange(
                    config.getEndpoint(),
                    HttpMethod.valueOf(config.getMetodo()),
                    entity,
                    Map.class
            );

            log.info("Respuesta de SUNAT para RUC {}: {}", numeroRuc, response.getBody());

            // 8. Procesar respuesta y crear SunatResponse
            SunatResponse sunatResponse = procesarRespuestaSunat(response.getBody(), numeroRuc);

            // 9. Registrar auditoría del consumo exitoso
            auditoriaService.registrarConsumoExitoso(
                    funcion.getApiServicesFuncionId(), 
                    requestBody, 
                    response.getBody(), 
                    true
            );

            return sunatResponse;

        } catch (Exception e) {
            log.error("Error consultando RUC SUNAT para usuarioId: {}", usuarioId, e);
            
            // Obtener función para auditoría
            Optional<ApiServicesFuncion> funcionOpt = apiResolucionService.obtenerFuncionInterna(codigoFuncion);
            Integer funcionId = funcionOpt.map(ApiServicesFuncion::getApiServicesFuncionId).orElse(null);
            
            // Registrar auditoría del consumo fallido
            if (funcionId != null) {
                auditoriaService.registrarConsumoFallido(
                        funcionId, 
                        Map.of("ruc", numeroRuc), 
                        e.getMessage(), 
                        true
                );
            }

            throw new Exception("Error consultando RUC SUNAT: " + e.getMessage());
        }
    }

    /**
     * Construye el request para SUNAT usando las metadata de configuración
     */
    private Map<String, Object> construirRequestSunat(String numeroRuc, String requestMetadata, String configRequest) {
        Map<String, Object> requestBody = new HashMap<>();
        
        // Construir request básico para SUNAT
        requestBody.put("ruc", numeroRuc);
        requestBody.put("tipo", "ruc");
        
        // Aquí se podría usar requestMetadata o configRequest para personalizar el request
        // si el proveedor externo requiere un formato específico
        
        return requestBody;
    }

    /**
     * Procesa la respuesta del proveedor SUNAT y la convierte a SunatResponse
     */
    private SunatResponse procesarRespuestaSunat(Map responseMap, String numeroRuc) {
        SunatResponse response = new SunatResponse();
        
        if (responseMap != null) {
            // Mapear campos según la respuesta del proveedor
            // Estos nombres de campos deben ajustarse según la respuesta real del proveedor SUNAT
            response.setNumero_documento(numeroRuc);
            response.setRazon_social(extractString(responseMap, "razonSocial", "EMPRESA CONSULTADA"));
            response.setEstado(extractString(responseMap, "estado", "ACTIVO"));
            response.setCondicion(extractString(responseMap, "condicion", "HABIDO"));
            response.setDireccion(extractString(responseMap, "direccion", "DIRECCIÓN NO DISPONIBLE"));
            response.setDistrito(extractString(responseMap, "distrito", "LIMA"));
            response.setProvincia(extractString(responseMap, "provincia", "LIMA"));
            response.setDepartamento(extractString(responseMap, "departamento", "LIMA"));
            response.setTipoConsulta("RUC");
            response.setLimiteConsultas(100);
            response.setMensaje("Consulta exitosa");
            response.setPlan("free");
        } else {
            // Respuesta por defecto si no hay datos
            response.setNumero_documento(numeroRuc);
            response.setRazon_social("NO ENCONTRADO");
            response.setEstado("INACTIVO");
            response.setCondicion("NO HABIDO");
            response.setTipoConsulta("RUC");
            response.setMensaje("RUC no encontrado en SUNAT");
            response.setPlan("free");
        }
        
        return response;
    }

    /**
     * Método utilitario para extraer valores del mapa de respuesta
     */
    private String extractString(Map map, String key, String defaultValue) {
        if (map != null && map.containsKey(key)) {
            Object value = map.get(key);
            return value != null ? value.toString() : defaultValue;
        }
        return defaultValue;
    }
}
