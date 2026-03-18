package com.extech.IntegracionesApis.Service;

import com.extech.IntegracionesApis.Domain.Model.Consumo;
import com.extech.IntegracionesApis.Repository.LogRepository;
import com.extech.IntegracionesApis.Util.Security.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Servicio de auditoría para registro de consumos en IT_Consumo
 * 
 * Este servicio centraliza el registro de todas las llamadas a servicios externos,
 * asegurando que se almacene la información necesaria para auditoría y control.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuditoriaService {

    private final LogRepository logRepository;
    private final ObjectMapper objectMapper;

    /**
     * Registra un consumo exitoso en la tabla IT_Consumo
     * 
     * @param apiServicesFuncionId ID de la función interna consumida
     * @param request Request enviado al proveedor externo (puede ser null)
     * @param response Response recibido del proveedor externo (puede ser null)
     * @param esConsulta Indica si es una consulta (true) o una mutación (false)
     */
    public void registrarConsumoExitoso(Integer apiServicesFuncionId, Object request, Object response, boolean esConsulta) {
        registrarConsumo(apiServicesFuncionId, request, response, true, esConsulta, null);
    }

    /**
     * Registra un consumo fallido en la tabla IT_Consumo
     * 
     * @param apiServicesFuncionId ID de la función interna consumida
     * @param request Request enviado al proveedor externo (puede ser null)
     * @param errorMessage Mensaje de error ocurrido
     * @param esConsulta Indica si es una consulta (true) o una mutación (false)
     */
    public void registrarConsumoFallido(Integer apiServicesFuncionId, Object request, String errorMessage, boolean esConsulta) {
        registrarConsumo(apiServicesFuncionId, request, null, false, esConsulta, errorMessage);
    }

    /**
     * Método principal para registrar consumos
     * 
     * @param apiServicesFuncionId ID de la función interna consumida
     * @param request Request enviado al proveedor externo
     * @param response Response recibido del proveedor externo
     * @param exito Indica si el consumo fue exitoso
     * @param esConsulta Indica si es una consulta (true) o una mutación (false)
     * @param errorMessage Mensaje de error (solo si exito es false)
     */
    private void registrarConsumo(Integer apiServicesFuncionId, Object request, Object response, 
                                 boolean exito, boolean esConsulta, String errorMessage) {
        
        try {
            if (apiServicesFuncionId == null) {
                // La BD requiere ApiServicesFuncionId NOT NULL. Si no hay función interna, no registramos auditoría.
                log.warn("Auditoría omitida: ApiServicesFuncionId es null (BD no permite null)");
                return;
            }

            // Obtener el usuarioId del contexto de seguridad
            Integer usuarioId = UserContext.getUsuarioId();
            if (usuarioId == null) {
                log.warn("No hay usuario autenticado en el contexto para registrar consumo");
                return;
            }

            Consumo consumo = new Consumo();
            consumo.setUsuarioId(usuarioId);
            consumo.setApiServicesFuncionId(apiServicesFuncionId);
            consumo.setExito(exito);
            consumo.setEsConsulta(esConsulta);
            consumo.setActivo(true);
            consumo.setEliminado(false);

            // Serializar request a JSON si no es null
            if (request != null) {
                try {
                    String requestJson = objectMapper.writeValueAsString(request);
                    // Limitar el tamaño para evitar exceder el campo de base de datos
                    if (requestJson.length() > 3800) {
                        requestJson = requestJson.substring(0, 3800) + "...[TRUNCADO]";
                    }
                    consumo.setRequest(requestJson);
                } catch (Exception e) {
                    log.warn("Error serializando request para auditoría", e);
                    consumo.setRequest("{\"error\":\"Error serializando request\"}");
                }
            }

            // Serializar response a JSON si no es null
            if (response != null) {
                try {
                    String responseJson = objectMapper.writeValueAsString(response);
                    // Limitar el tamaño para evitar exceder el campo de base de datos
                    if (responseJson.length() > 3800) {
                        responseJson = responseJson.substring(0, 3800) + "...[TRUNCADO]";
                    }
                    consumo.setResponse(responseJson);
                } catch (Exception e) {
                    log.warn("Error serializando response para auditoría", e);
                    consumo.setResponse("{\"error\":\"Error serializando response\"}");
                }
            } else if (!exito && errorMessage != null) {
                // Si hay error, registrar el mensaje de error en el response
                consumo.setResponse("{\"error\":\"" + errorMessage.replace("\"", "\\\"") + "\"}");
            }

            // Guardar el consumo
            logRepository.save(consumo);
            
            log.info("Consumo registrado - UsuarioId: {}, FunciónId: {}, Éxito: {}, Consulta: {}", 
                    usuarioId, apiServicesFuncionId, exito, esConsulta);

        } catch (Exception e) {
            log.error("Error registrando consumo en auditoría", e);
            // No lanzar excepción para no afectar el flujo principal
        }
    }

    /**
     * Método de conveniencia para registrar consumo con strings directamente
     * 
     * @param apiServicesFuncionId ID de la función interna consumida
     * @param requestString Request en formato string
     * @param responseString Response en formato string
     * @param exito Indica si el consumo fue exitoso
     * @param esConsulta Indica si es una consulta (true) o una mutación (false)
     */
    public void registrarConsumoConStrings(Integer apiServicesFuncionId, String requestString, 
                                         String responseString, boolean exito, boolean esConsulta) {
        
        try {
            Integer usuarioId = UserContext.getUsuarioId();
            if (usuarioId == null) {
                log.warn("No hay usuario autenticado en el contexto para registrar consumo");
                return;
            }

            Consumo consumo = new Consumo();
            consumo.setUsuarioId(usuarioId);
            consumo.setApiServicesFuncionId(apiServicesFuncionId);
            consumo.setExito(exito);
            consumo.setEsConsulta(esConsulta);
            consumo.setActivo(true);
            consumo.setEliminado(false);

            // Limitar tamaño de los strings
            if (requestString != null && requestString.length() > 3800) {
                requestString = requestString.substring(0, 3800) + "...[TRUNCADO]";
            }
            consumo.setRequest(requestString);

            if (responseString != null && responseString.length() > 3800) {
                responseString = responseString.substring(0, 3800) + "...[TRUNCADO]";
            }
            consumo.setResponse(responseString);

            logRepository.save(consumo);
            
            log.info("Consumo registrado - UsuarioId: {}, FunciónId: {}, Éxito: {}, Consulta: {}", 
                    usuarioId, apiServicesFuncionId, exito, esConsulta);

        } catch (Exception e) {
            log.error("Error registrando consumo en auditoría", e);
        }
    }
}
