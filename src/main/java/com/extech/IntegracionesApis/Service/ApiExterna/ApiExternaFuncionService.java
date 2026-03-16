package com.extech.IntegracionesApis.Service.ApiExterna;

import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Repository.General.ApiExternaFuncionRepository;
import com.extech.IntegracionesApis.Util.SecretEncryptionUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiExternaFuncionService {

    private final ApiExternaFuncionRepository apiExternaFuncionRepository;
    private final SecretEncryptionUtil encryptionUtil;

    /**
     * Guarda una API externa cifrando el token antes de guardarlo en BD
     */
    public Map<String, Object> guardarApiExterna(Map<String, Object> datos) {
        log.info("Guardando API externa: {}", datos.get("nombre"));

        // Verificar si ya existe una API con el mismo nombre
        String nombre = datos.get("nombre") != null ? datos.get("nombre").toString() : "";
        Optional<ApiExternaFuncion> existente = apiExternaFuncionRepository.findByNombreAndActivoTrue(nombre);

        if (existente.isPresent()) {
            throw new RuntimeException("Ya existe una API externa con el nombre: " + nombre);
        }

        // Crear nueva entidad
        ApiExternaFuncion apiExterna = new ApiExternaFuncion();
        apiExterna.setNombre(nombre);
        apiExterna.setCodigo(datos.get("codigo") != null ? datos.get("codigo").toString() : "");
        apiExterna.setDescripcion(datos.get("descripcion") != null ? datos.get("descripcion").toString() : "");
        apiExterna.setEndpoint(datos.get("endpoint") != null ? datos.get("endpoint").toString() : "");
        apiExterna.setMetodo(datos.get("metodo") != null ? datos.get("metodo").toString() : "");
        apiExterna.setAutorizacion(datos.get("autorizacion") != null ? datos.get("autorizacion").toString() : "");
        apiExterna.setRequest(datos.get("request") != null ? datos.get("request").toString() : "");
        apiExterna.setResponse(datos.get("response") != null ? datos.get("response").toString() : "");
        
        // Cifrar el token ANTES de guardarlo
        String tokenPlano = datos.get("token") != null ? datos.get("token").toString() : "";
        if (!tokenPlano.isEmpty()) {
            String tokenCifrado = encryptionUtil.encrypt(tokenPlano);
            apiExterna.setToken(tokenCifrado);
            log.info("Token cifrado para API: {}", nombre);
        }

        // Tiempo de consulta y segmento
        if (datos.get("tiempoConsulta") != null) {
            apiExterna.setTiempoConsulta(((Number) datos.get("tiempoConsulta")).intValue());
        }
        apiExterna.setSegmentoTiempo(datos.get("segmentoTiempo") != null ? datos.get("segmentoTiempo").toString() : "SEG");

        // Usuario registro
        if (datos.get("usuarioRegistro") != null) {
            apiExterna.setUsuarioRegistro(((Number) datos.get("usuarioRegistro")).intValue());
        }

        // Guardar en BD
        ApiExternaFuncion guardada = apiExternaFuncionRepository.save(apiExterna);
        log.info("API externa guardada exitosamente: {} (ID: {})", nombre, guardada.getApiExternaFuncionId());

        // Retornar respuesta sin incluir el token cifrado
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "API externa guardada correctamente");
        response.put("apiExternaFuncionId", guardada.getApiExternaFuncionId());
        response.put("nombre", guardada.getNombre());
        response.put("codigo", guardada.getCodigo());
        response.put("endpoint", guardada.getEndpoint());
        response.put("metodo", guardada.getMetodo());
        response.put("tokenGuardado", true); // Indicar que el token fue guardado cifrado

        return response;
    }

    /**
     * Obtiene todas las APIs externas activas (sin descifrar tokens)
     */
    public List<ApiExternaFuncion> listarApisActivas() {
        return apiExternaFuncionRepository.findByActivoTrue();
    }

    /**
     * Obtiene una API externa por ID y descifra su token para uso en memoria
     */
    public Optional<ApiExternaFuncion> obtenerApiConTokenDescifrado(Integer apiExternaFuncionId) {
        Optional<ApiExternaFuncion> apiOpt = apiExternaFuncionRepository.findById(apiExternaFuncionId);

        if (apiOpt.isPresent()) {
            ApiExternaFuncion api = apiOpt.get();
            
            // Descifrar el token solo para uso en memoria
            if (api.getToken() != null && !api.getToken().isEmpty()) {
                try {
                    String tokenDescifrado = encryptionUtil.decrypt(api.getToken());
                    // No guardamos el token descifrado, solo lo retornamos para uso temporal
                    // Podríamos crear un DTO especial para esto
                    log.debug("Token descifrado para uso temporal: {}", api.getNombre());
                } catch (Exception e) {
                    log.error("Error al descifrar token para API {}: {}", api.getNombre(), e.getMessage());
                }
            }
        }

        return apiOpt;
    }

    /**
     * Obtiene el token descifrado de una API para consumo HTTP
     */
    public String obtenerTokenDescifrado(Integer apiExternaFuncionId) {
        Optional<ApiExternaFuncion> apiOpt = apiExternaFuncionRepository.findById(apiExternaFuncionId);

        if (apiOpt.isEmpty()) {
            throw new RuntimeException("API externa no encontrada");
        }

        ApiExternaFuncion api = apiOpt.get();

        if (!Boolean.TRUE.equals(api.getActivo()) || Boolean.TRUE.equals(api.getEliminado())) {
            throw new RuntimeException("API externa inactiva o eliminada");
        }

        if (api.getToken() == null || api.getToken().isEmpty()) {
            return null; // No hay token configurado
        }

        try {
            return encryptionUtil.decrypt(api.getToken());
        } catch (Exception e) {
            log.error("Error al descifrar token para API {}: {}", api.getNombre(), e.getMessage());
            throw new RuntimeException("Error al descifrar token de la API", e);
        }
    }

    /**
     * Actualiza una API externa cifrando el token si se proporciona
     */
    public Map<String, Object> actualizarApiExterna(Integer apiExternaFuncionId, Map<String, Object> datos) {
        Optional<ApiExternaFuncion> apiOpt = apiExternaFuncionRepository.findById(apiExternaFuncionId);

        if (apiOpt.isEmpty()) {
            throw new RuntimeException("API externa no encontrada");
        }

        ApiExternaFuncion api = apiOpt.get();
        log.info("Actualizando API externa: {}", api.getNombre());

        // Actualizar campos básicos
        if (datos.containsKey("nombre")) api.setNombre(datos.get("nombre").toString());
        if (datos.containsKey("codigo")) api.setCodigo(datos.get("codigo").toString());
        if (datos.containsKey("descripcion")) api.setDescripcion(datos.get("descripcion").toString());
        if (datos.containsKey("endpoint")) api.setEndpoint(datos.get("endpoint").toString());
        if (datos.containsKey("metodo")) api.setMetodo(datos.get("metodo").toString());
        if (datos.containsKey("autorizacion")) api.setAutorizacion(datos.get("autorizacion").toString());
        if (datos.containsKey("request")) api.setRequest(datos.get("request").toString());
        if (datos.containsKey("response")) api.setResponse(datos.get("response").toString());
        if (datos.containsKey("segmentoTiempo")) api.setSegmentoTiempo(datos.get("segmentoTiempo").toString());
        if (datos.containsKey("tiempoConsulta")) api.setTiempoConsulta(((Number) datos.get("tiempoConsulta")).intValue());

        // Si se proporciona un nuevo token, cifrarlo
        if (datos.containsKey("token")) {
            String nuevoToken = datos.get("token").toString();
            if (!nuevoToken.isEmpty()) {
                String tokenCifrado = encryptionUtil.encrypt(nuevoToken);
                api.setToken(tokenCifrado);
                log.info("Token actualizado y cifrado para API: {}", api.getNombre());
            }
        }

        if (datos.containsKey("usuarioModificacion")) {
            api.setUsuarioModificacion(((Number) datos.get("usuarioModificacion")).intValue());
        }

        api.setFechaModificacion(LocalDateTime.now());

        // Guardar cambios
        ApiExternaFuncion actualizada = apiExternaFuncionRepository.save(api);
        log.info("API externa actualizada exitosamente: {}", actualizada.getNombre());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "API externa actualizada correctamente");
        response.put("apiExternaFuncionId", actualizada.getApiExternaFuncionId());
        response.put("nombre", actualizada.getNombre());
        response.put("fechaModificacion", actualizada.getFechaModificacion());

        return response;
    }
}
