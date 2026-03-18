package com.extech.IntegracionesApis.Service;

import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiServicesFuncion;
import com.extech.IntegracionesApis.Repository.ApiConfiguracionRepository;
import com.extech.IntegracionesApis.Repository.ApiExternaResolucionProjection;
import com.extech.IntegracionesApis.Repository.General.ApiAsignacionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiExternaFuncionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiServicesFuncionRepository;
import com.extech.IntegracionesApis.Util.SecretEncryptionUtil;
import com.extech.IntegracionesApis.Util.Security.TokenEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Servicio de resolución de configuración de API externa
 * 
 * Este servicio implementa el flujo principal para obtener la configuración
 * del proveedor externo basado en el usuario autenticado y la función solicitada.
 * 
 * Flujo:
 * TokenUsuario -> Usuario autenticado -> Función interna solicitada -> 
 * SP uspObtenerConfiguracionApiExternaPorUsuario -> Configuración externa
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ApiResolucionService {

    private final ApiConfiguracionRepository apiConfiguracionRepository;
    private final ApiAsignacionRepository apiAsignacionRepository;
    private final ApiExternaFuncionRepository apiExternaFuncionRepository;
    private final ApiServicesFuncionRepository apiServicesFuncionRepository;
    private final TokenEncryptionUtil tokenEncryptionUtil;
    private final SecretEncryptionUtil secretEncryptionUtil;

    /**
     * Resuelve la configuración completa del proveedor externo
     * 
     * @param usuarioId ID del usuario autenticado
     * @param codigoFuncion Código de la función interna (ej: SMS_SEND, RENIEC_DNI, SUNAT_RUC)
     * @return Optional con la configuración del proveedor externo o empty si no encuentra
     */
    public Optional<ApiExternaFuncion> resolverConfiguracionExterna(Integer usuarioId, String codigoFuncion) {
        log.info("Resolviendo configuración externa para usuarioId: {} y función: {}", usuarioId, codigoFuncion);
        
        try {
            // Ejecutar el SP para obtener la configuración completa
            Optional<ApiExternaResolucionProjection> configOpt = apiConfiguracionRepository
                    .obtenerConfiguracionPorUsuarioYFuncion(usuarioId, codigoFuncion);
            
            if (configOpt.isPresent()) {
                ApiExternaResolucionProjection row = configOpt.get();

                ApiExternaFuncion config = new ApiExternaFuncion();
                config.setApiExternaFuncionId(row.getApiExternaFuncionId());
                config.setNombre(row.getNombreFuncionExterna());
                config.setCodigo(row.getCodigoFuncionExterna());
                config.setEndpoint(row.getEndpointExterno());
                config.setMetodo(row.getMetodoExterno());
                config.setToken(row.getToken());
                config.setAutorizacion(row.getAutorizacion());
                config.setRequest(row.getRequest());
                config.setResponse(row.getResponse());
                config.setTiempoConsulta(row.getTiempoConsulta());
                config.setSegmentoTiempo(row.getSegmentoTiempo());

                log.info("Configuración encontrada (SP) - Endpoint: {}, Método: {}",
                        config.getEndpoint(), config.getMetodo());
                return Optional.of(config);
            }

            // Fallback 1: Resolver por JOIN en código (equivalente al SP) usando ApiServicesFuncion -> ApiAsignacion -> ApiExternaFuncion
            Optional<ApiServicesFuncion> funcionInterna = apiServicesFuncionRepository.findByCodigo(codigoFuncion);
            if (funcionInterna.isPresent()) {
                Integer apiServicesFuncionId = funcionInterna.get().getApiServicesFuncionId();
                var asignaciones = apiAsignacionRepository.findByApiServicesFuncionId(apiServicesFuncionId);
                if (asignaciones != null && !asignaciones.isEmpty()) {
                    Integer apiExternaFuncionId = asignaciones.get(0).getApiExternaFuncionId();
                    Optional<ApiExternaFuncion> externaOpt = apiExternaFuncionRepository
                            .findByApiExternaFuncionIdAndActivoTrueAndEliminadoFalse(apiExternaFuncionId);
                    if (externaOpt.isPresent()) {
                        ApiExternaFuncion config = externaOpt.get();
                        log.warn("Configuración encontrada por JOIN en código - Usuario: {}, Función: {}, ApiExternaFuncionId: {}",
                                usuarioId, codigoFuncion, apiExternaFuncionId);
                        return externaOpt;
                    }
                }
            }

            // Fallback 2: permitir configuración global por código (sin asignación por usuario)
            Optional<ApiExternaFuncion> fallback = apiExternaFuncionRepository
                    .findByCodigoAndActivoTrueAndEliminadoFalse(codigoFuncion);
            if (fallback.isPresent()) {
                ApiExternaFuncion config = fallback.get();
                log.warn("Usando configuración GLOBAL por código {} (sin asignación por usuario {}) - Endpoint: {}, Método: {}",
                        codigoFuncion, usuarioId, config.getEndpoint(), config.getMetodo());
                return fallback;
            }

            log.warn("No se encontró configuración para usuarioId: {} y función: {}", usuarioId, codigoFuncion);
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error resolviendo configuración externa para usuarioId: {} y función: {}", 
                    usuarioId, codigoFuncion, e);
            // Si el SP falla, intentar resolver por JOIN en código primero, luego fallback por código.
            try {
                Optional<ApiServicesFuncion> funcionInterna = apiServicesFuncionRepository.findByCodigo(codigoFuncion);
                if (funcionInterna.isPresent()) {
                    Integer apiServicesFuncionId = funcionInterna.get().getApiServicesFuncionId();
                    var asignaciones = apiAsignacionRepository.findByApiServicesFuncionId(apiServicesFuncionId);
                    if (asignaciones != null && !asignaciones.isEmpty()) {
                        Integer apiExternaFuncionId = asignaciones.get(0).getApiExternaFuncionId();
                        Optional<ApiExternaFuncion> externaOpt = apiExternaFuncionRepository
                                .findByApiExternaFuncionIdAndActivoTrueAndEliminadoFalse(apiExternaFuncionId);
                        if (externaOpt.isPresent()) {
                            ApiExternaFuncion config = externaOpt.get();
                            log.warn("Configuración encontrada por JOIN en código tras fallo del SP - ApiExternaFuncionId: {}",
                                    apiExternaFuncionId);
                            return externaOpt;
                        }
                    }
                }
            } catch (Exception ignored) {
            }

            // Si todo falla, intentar fallback global por código
            Optional<ApiExternaFuncion> fallback = apiExternaFuncionRepository
                    .findByCodigoAndActivoTrueAndEliminadoFalse(codigoFuncion);
            if (fallback.isPresent()) {
                ApiExternaFuncion config = fallback.get();
                log.warn("Usando configuración GLOBAL por código {} tras fallo del SP - Endpoint: {}, Método: {}",
                        codigoFuncion, config.getEndpoint(), config.getMetodo());
                return fallback;
            }
            return Optional.empty();
        }
    }

    /**
     * Obtiene la función interna por su código
     * 
     * @param codigoFuncion Código de la función interna
     * @return Optional con la función interna o empty si no encuentra
     */
    public Optional<ApiServicesFuncion> obtenerFuncionInterna(String codigoFuncion) {
        log.debug("Buscando función interna con código: {}", codigoFuncion);
        return apiServicesFuncionRepository.findByCodigo(codigoFuncion);
    }

    /**
     * Descifra el token del proveedor externo
     * 
     * @param tokenCifrado Token cifrado almacenado en base de datos
     * @return Token descifrado o null si hay error
     */
    public String descifrarTokenExterno(String tokenCifrado) {
        if (tokenCifrado == null || tokenCifrado.trim().isEmpty()) {
            log.warn("Token cifrado es nulo o vacío");
            return null;
        }
        
        try {
            String tokenDescifrado = tokenEncryptionUtil.decrypt(tokenCifrado);
            log.debug("Token externo descifrado correctamente");
            return tokenDescifrado;
        } catch (Exception e) {
            // Intentar con AES-GCM (SecretEncryptionUtil) si el token fue cifrado con esa utilidad
            try {
                String tokenDescifrado = secretEncryptionUtil.decrypt(tokenCifrado);
                log.debug("Token externo descifrado correctamente (AES-GCM)");
                return tokenDescifrado;
            } catch (Exception ignored) {
                // Como último fallback, asumir que el token está en claro
                log.warn("No se pudo descifrar token externo; usando valor tal cual");
                return tokenCifrado;
            }
        }
    }

    /**
     * Valida que la configuración esté completa para consumir el servicio externo
     * 
     * @param config Configuración del proveedor externo
     * @return true si la configuración es válida, false si falta información
     */
    public boolean validarConfiguracionCompleta(ApiExternaFuncion config) {
        if (config == null) {
            log.error("Configuración es nula");
            return false;
        }

        if (config.getEndpoint() == null || config.getEndpoint().trim().isEmpty()) {
            log.error("Endpoint es requerido");
            return false;
        }

        if (config.getMetodo() == null || config.getMetodo().trim().isEmpty()) {
            log.error("Método HTTP es requerido");
            return false;
        }

        // El token puede ser opcional si la autenticación es por otros medios
        if (config.getAutorizacion() == null || config.getAutorizacion().trim().isEmpty()) {
            log.warn("Autorización no configurada, podría ser requerida según el proveedor");
        }

        log.debug("Configuración validada correctamente");
        return true;
    }
}
