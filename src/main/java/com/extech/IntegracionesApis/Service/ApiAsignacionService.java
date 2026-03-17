package com.extech.IntegracionesApis.Service;

import com.extech.IntegracionesApis.Domain.Model.ApiAsignacion;
import com.extech.IntegracionesApis.Domain.Model.ApiExternaFuncion;
import com.extech.IntegracionesApis.Domain.Model.ApiServicesFuncion;
import com.extech.IntegracionesApis.Repository.General.ApiAsignacionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiExternaFuncionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiServicesFuncionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiAsignacionService {

    private final ApiAsignacionRepository apiAsignacionRepository;
    private final ApiServicesFuncionRepository apiServicesFuncionRepository;
    private final ApiExternaFuncionRepository apiExternaFuncionRepository;

    /**
     * Crea una nueva asignacion entre API Service y API Externa
     */
    @Transactional
    public Map<String, Object> crearAsignacion(Map<String, Object> datos) {
        log.info("Creando asignacion API");

        // Validar que existan las APIs
        Integer apiServicesFuncionId = ((Number) datos.get("apiServicesFuncionId")).intValue();
        Integer apiExternaFuncionId = ((Number) datos.get("apiExternaFuncionId")).intValue();

        Optional<ApiServicesFuncion> serviceOpt = apiServicesFuncionRepository.findById(apiServicesFuncionId);
        if (serviceOpt.isEmpty()) {
            throw new RuntimeException("API Service Function no encontrada con ID: " + apiServicesFuncionId);
        }

        Optional<ApiExternaFuncion> externaOpt = apiExternaFuncionRepository.findById(apiExternaFuncionId);
        if (externaOpt.isEmpty()) {
            throw new RuntimeException("API Externa Function no encontrada con ID: " + apiExternaFuncionId);
        }

        // Verificar que no exista una asignacion duplicada
        if (apiAsignacionRepository.existsByApiServicesFuncionIdAndApiExternaFuncionIdAndActivoTrueAndEliminadoFalse(
                apiServicesFuncionId, apiExternaFuncionId)) {
            throw new RuntimeException("Ya existe una asignacion activa entre estas APIs");
        }

        // Crear nueva asignacion
        ApiAsignacion asignacion = new ApiAsignacion();
        asignacion.setApiServicesFuncionId(apiServicesFuncionId);
        asignacion.setApiExternaFuncionId(apiExternaFuncionId);

        if (datos.containsKey("usuarioRegistro")) {
            asignacion.setUsuarioRegistro(((Number) datos.get("usuarioRegistro")).intValue());
        }

        ApiAsignacion guardada = apiAsignacionRepository.save(asignacion);
        log.info("Asignacion creada exitosamente: {} -> {}", 
                serviceOpt.get().getNombre(), externaOpt.get().getNombre());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Asignacion creada correctamente");
        response.put("apiAsignacionId", guardada.getApiAsignacionId());
        response.put("apiServicesFuncionId", guardada.getApiServicesFuncionId());
        response.put("apiExternaFuncionId", guardada.getApiExternaFuncionId());
        response.put("fechaRegistro", guardada.getFechaRegistro());

        return response;
    }

    /**
     * Obtiene todas las asignaciones activas
     */
    public List<ApiAsignacion> listarAsignacionesActivas() {
        return apiAsignacionRepository.findActivos();
    }

    /**
     * Obtiene asignaciones por API Service Function
     */
    public List<ApiAsignacion> obtenerPorApiServicesFuncion(Integer apiServicesFuncionId) {
        return apiAsignacionRepository.findByApiServicesFuncionId(apiServicesFuncionId);
    }

    /**
     * Obtiene asignaciones por API Externa Function
     */
    public List<ApiAsignacion> obtenerPorApiExternaFuncion(Integer apiExternaFuncionId) {
        return apiAsignacionRepository.findByApiExternaFuncionId(apiExternaFuncionId);
    }

    /**
     * Obtiene una asignacion especifica por ambas APIs
     */
    public Optional<ApiAsignacion> obtenerAsignacionEspecifica(
            Integer apiServicesFuncionId, Integer apiExternaFuncionId) {
        return apiAsignacionRepository.findByApiServicesFuncionIdAndApiExternaFuncionId(
                apiServicesFuncionId, apiExternaFuncionId);
    }

    /**
     * Elimina logicamente una asignacion
     */
    @Transactional
    public Map<String, Object> eliminarAsignacion(Integer apiAsignacionId, Integer usuarioModificacion) {
        Optional<ApiAsignacion> asignacionOpt = apiAsignacionRepository.findById(apiAsignacionId);

        if (asignacionOpt.isEmpty()) {
            throw new RuntimeException("Asignacion no encontrada con ID: " + apiAsignacionId);
        }

        ApiAsignacion asignacion = asignacionOpt.get();
        
        if (Boolean.TRUE.equals(asignacion.getEliminado())) {
            throw new RuntimeException("La asignacion ya esta eliminada");
        }

        asignacion.setEliminado(true);
        asignacion.setActivo(false);
        asignacion.setUsuarioModificacion(usuarioModificacion);
        asignacion.setFechaModificacion(LocalDateTime.now());

        apiAsignacionRepository.save(asignacion);
        log.info("Asignacion eliminada logicamente: {}", apiAsignacionId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Asignacion eliminada correctamente");
        response.put("apiAsignacionId", apiAsignacionId);
        response.put("fechaModificacion", asignacion.getFechaModificacion());

        return response;
    }

    /**
     * Reactiva una asignacion previamente eliminada
     */
    @Transactional
    public Map<String, Object> reactivarAsignacion(Integer apiAsignacionId, Integer usuarioModificacion) {
        Optional<ApiAsignacion> asignacionOpt = apiAsignacionRepository.findById(apiAsignacionId);

        if (asignacionOpt.isEmpty()) {
            throw new RuntimeException("Asignacion no encontrada con ID: " + apiAsignacionId);
        }

        ApiAsignacion asignacion = asignacionOpt.get();
        
        if (!Boolean.TRUE.equals(asignacion.getEliminado())) {
            throw new RuntimeException("La asignacion no esta eliminada");
        }

        asignacion.setEliminado(false);
        asignacion.setActivo(true);
        asignacion.setUsuarioModificacion(usuarioModificacion);
        asignacion.setFechaModificacion(LocalDateTime.now());

        apiAsignacionRepository.save(asignacion);
        log.info("Asignacion reactivada: {}", apiAsignacionId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Asignacion reactivada correctamente");
        response.put("apiAsignacionId", apiAsignacionId);
        response.put("fechaModificacion", asignacion.getFechaModificacion());

        return response;
    }

    /**
     * Obtiene detalles completos de una asignacion con informacion de las APIs relacionadas
     */
    public Map<String, Object> obtenerDetallesAsignacion(Integer apiAsignacionId) {
        Optional<ApiAsignacion> asignacionOpt = apiAsignacionRepository.findById(apiAsignacionId);

        if (asignacionOpt.isEmpty()) {
            throw new RuntimeException("Asignacion no encontrada con ID: " + apiAsignacionId);
        }

        ApiAsignacion asignacion = asignacionOpt.get();
        
        // Obtener informacion de las APIs relacionadas
        Optional<ApiServicesFuncion> serviceOpt = apiServicesFuncionRepository.findById(
                asignacion.getApiServicesFuncionId());
        Optional<ApiExternaFuncion> externaOpt = apiExternaFuncionRepository.findById(
                asignacion.getApiExternaFuncionId());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("asignacion", asignacion);
        
        if (serviceOpt.isPresent()) {
            Map<String, Object> serviceInfo = new LinkedHashMap<>();
            serviceInfo.put("id", serviceOpt.get().getApiServicesFuncionId());
            serviceInfo.put("nombre", serviceOpt.get().getNombre());
            serviceInfo.put("codigo", serviceOpt.get().getCodigo());
            serviceInfo.put("endpoint", serviceOpt.get().getEndpoint());
            response.put("apiService", serviceInfo);
        }

        if (externaOpt.isPresent()) {
            Map<String, Object> externaInfo = new LinkedHashMap<>();
            externaInfo.put("id", externaOpt.get().getApiExternaFuncionId());
            externaInfo.put("nombre", externaOpt.get().getNombre());
            externaInfo.put("codigo", externaOpt.get().getCodigo());
            externaInfo.put("endpoint", externaOpt.get().getEndpoint());
            response.put("apiExterna", externaInfo);
        }

        return response;
    }
}
