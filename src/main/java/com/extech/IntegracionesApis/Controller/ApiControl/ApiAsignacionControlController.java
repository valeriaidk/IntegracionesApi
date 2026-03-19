package com.extech.IntegracionesApis.Controller.ApiControl;

import com.extech.IntegracionesApis.Service.ApiAsignacionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/asignaciones-control")
@RequiredArgsConstructor
@Slf4j
public class ApiAsignacionControlController {

    private final ApiAsignacionService apiAsignacionService;

    /**
     * Crea una nueva asignación entre API Service y API Externa
     */
    @PostMapping("/crear")
    public ResponseEntity<?> crearAsignacion(@RequestBody Map<String, Object> body) {
        try {
            Map<String, Object> response = apiAsignacionService.crearAsignacion(body);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error al crear asignación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        }
    }

    /**
     * Lista todas las asignaciones activas
     */
    @GetMapping("/listar")
    public ResponseEntity<?> listarAsignacionesActivas() {
        try {
            List<?> asignaciones = apiAsignacionService.listarAsignacionesActivas();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", asignaciones,
                "count", asignaciones.size()
            ));
        } catch (Exception e) {
            log.error("Error al listar asignaciones: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error al listar asignaciones"
            ));
        }
    }

    /**
     * Obtiene asignaciones por API Service Function
     */
    @GetMapping("/por-service/{apiServicesFuncionId}")
    public ResponseEntity<?> obtenerPorApiServicesFuncion(@PathVariable Integer apiServicesFuncionId) {
        try {
            List<?> asignaciones = apiAsignacionService.obtenerPorApiServicesFuncion(apiServicesFuncionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", asignaciones,
                "apiServicesFuncionId", apiServicesFuncionId,
                "count", asignaciones.size()
            ));
        } catch (Exception e) {
            log.error("Error al obtener asignaciones por service: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error al obtener asignaciones por API Service"
            ));
        }
    }

    /**
     * Obtiene asignaciones por API Externa Function
     */
    @GetMapping("/por-externa/{apiExternaFuncionId}")
    public ResponseEntity<?> obtenerPorApiExternaFuncion(@PathVariable Integer apiExternaFuncionId) {
        try {
            List<?> asignaciones = apiAsignacionService.obtenerPorApiExternaFuncion(apiExternaFuncionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", asignaciones,
                "apiExternaFuncionId", apiExternaFuncionId,
                "count", asignaciones.size()
            ));
        } catch (Exception e) {
            log.error("Error al obtener asignaciones por externa: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error al obtener asignaciones por API Externa"
            ));
        }
    }

    /**
     * Obtiene una asignación específica por ambas APIs
     */
    @GetMapping("/especifica")
    public ResponseEntity<?> obtenerAsignacionEspecifica(
            @RequestParam Integer apiServicesFuncionId,
            @RequestParam Integer apiExternaFuncionId) {
        try {
            var asignacion = apiAsignacionService.obtenerAsignacionEspecifica(
                    apiServicesFuncionId, apiExternaFuncionId);
            
            if (asignacion.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", asignacion.get()
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "success", false,
                    "message", "No se encontró la asignación especificada"
                ));
            }
        } catch (Exception e) {
            log.error("Error al obtener asignación específica: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error al obtener asignación específica"
            ));
        }
    }

    /**
     * Obtiene detalles completos de una asignación con información de las APIs relacionadas
     */
    @GetMapping("/{apiAsignacionId}/detalles")
    public ResponseEntity<?> obtenerDetallesAsignacion(@PathVariable Integer apiAsignacionId) {
        try {
            Map<String, Object> detalles = apiAsignacionService.obtenerDetallesAsignacion(apiAsignacionId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "data", detalles
            ));
        } catch (RuntimeException e) {
            log.error("Error al obtener detalles de asignación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error interno al obtener detalles: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Elimina lógicamente una asignación
     */
    @DeleteMapping("/{apiAsignacionId}")
    public ResponseEntity<?> eliminarAsignacion(
            @PathVariable Integer apiAsignacionId,
            @RequestParam(required = false) Integer usuarioModificacion) {
        try {
            // Si no se proporciona usuarioModificacion, usar un valor por defecto
            Integer usuario = usuarioModificacion != null ? usuarioModificacion : 1;
            
            Map<String, Object> response = apiAsignacionService.eliminarAsignacion(apiAsignacionId, usuario);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error al eliminar asignación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error interno al eliminar asignación: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }

    /**
     * Reactiva una asignación previamente eliminada
     */
    @PostMapping("/{apiAsignacionId}/reactivar")
    public ResponseEntity<?> reactivarAsignacion(
            @PathVariable Integer apiAsignacionId,
            @RequestParam(required = false) Integer usuarioModificacion) {
        try {
            // Si no se proporciona usuarioModificacion, usar un valor por defecto
            Integer usuario = usuarioModificacion != null ? usuarioModificacion : 1;
            
            Map<String, Object> response = apiAsignacionService.reactivarAsignacion(apiAsignacionId, usuario);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            log.error("Error al reactivar asignación: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Error interno al reactivar asignación: {}", e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "Error interno del servidor"
            ));
        }
    }
}
