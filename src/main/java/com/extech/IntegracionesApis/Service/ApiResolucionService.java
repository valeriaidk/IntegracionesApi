package com.extech.IntegracionesApis.Service;

import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiServicesFuncion;
import com.extech.IntegracionesApis.Repository.ApiConfiguracionRepository;
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
    private final TokenEncryptionUtil tokenEncryptionUtil;

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
            Optional<ApiExternaFuncion> configOpt = apiConfiguracionRepository
                    .obtenerConfiguracionPorUsuarioYFuncion(usuarioId, codigoFuncion);
            
            if (configOpt.isPresent()) {
                ApiExternaFuncion config = configOpt.get();
                log.info("Configuración encontrada - Endpoint: {}, Método: {}", 
                        config.getEndpoint(), config.getMetodo());
                return configOpt;
            } else {
                log.warn("No se encontró configuración para usuarioId: {} y función: {}", usuarioId, codigoFuncion);
                return Optional.empty();
            }
            
        } catch (Exception e) {
            log.error("Error resolviendo configuración externa para usuarioId: {} y función: {}", 
                    usuarioId, codigoFuncion, e);
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
        
        return apiConfiguracionRepository.findByCodigoAndActivoTrue(codigoFuncion);
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
            log.error("Error descifrando token externo", e);
            return null;
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
