package com.extech.IntegracionesApis.Controller.Admin;

import com.extech.IntegracionesApis.Repository.ApiConfiguracionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiAsignacionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiExternaFuncionRepository;
import com.extech.IntegracionesApis.Repository.General.ApiServicesFuncionRepository;
import com.extech.IntegracionesApis.Service.ApiResolucionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/debug")
@RequiredArgsConstructor
public class DebugResolucionController {

    private final ApiResolucionService apiResolucionService;
    private final ApiServicesFuncionRepository apiServicesFuncionRepository;
    private final ApiAsignacionRepository apiAsignacionRepository;
    private final ApiExternaFuncionRepository apiExternaFuncionRepository;
    private final ApiConfiguracionRepository apiConfiguracionRepository;

    @GetMapping("/resolver")
    public ResponseEntity<?> debugResolver(
            @RequestParam Integer usuarioId,
            @RequestParam String codigoFuncion
    ) {
        Map<String, Object> out = new LinkedHashMap<>();

        out.put("usuarioId", usuarioId);
        out.put("codigoFuncion", codigoFuncion);

        var funcionOpt = apiServicesFuncionRepository.findByCodigo(codigoFuncion);
        out.put("funcionInternaExiste", funcionOpt.isPresent());
        out.put("apiServicesFuncionId", funcionOpt.map(f -> f.getApiServicesFuncionId()).orElse(null));

        if (funcionOpt.isPresent()) {
            var asignaciones = apiAsignacionRepository.findByApiServicesFuncionId(funcionOpt.get().getApiServicesFuncionId());
            out.put("asignacionesCount", asignaciones.size());
            out.put("asignaciones", asignaciones);
        }

        out.put("configGlobalPorCodigoExiste",
                apiExternaFuncionRepository.findByCodigoAndActivoTrueAndEliminadoFalse(codigoFuncion).isPresent());

        // Intentar resolver por servicio (incluye SP + fallbacks)
        out.put("resolverConfiguracionExterna",
                apiResolucionService.resolverConfiguracionExterna(usuarioId, codigoFuncion).orElse(null));

        // Intentar solo SP (para ver si devuelve algo)
        try {
            out.put("spRow", apiConfiguracionRepository.obtenerConfiguracionPorUsuarioYFuncion(usuarioId, codigoFuncion).orElse(null));
        } catch (Exception e) {
            out.put("spError", e.getMessage());
        }

        return ResponseEntity.ok(out);
    }
}

