package com.extech.IntegracionesApis.Service.Auth;

import com.extech.IntegracionesApis.Domain.Model.PlanUsuario;
import com.extech.IntegracionesApis.Domain.Model.TokenUsuario;
import com.extech.IntegracionesApis.Repository.User.PlanUsuarioRepository;
import com.extech.IntegracionesApis.Repository.User.TokenUsuarioRepository;
import com.extech.IntegracionesApis.Util.Security.PasswordHashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyService {

    private final TokenUsuarioRepository tokenUsuarioRepository;
    private final PlanUsuarioRepository planUsuarioRepository;
    private final PasswordHashUtil passwordHashUtil;

    public Map<String, Object> generarApiKey(Integer usuarioId) {

        Optional<PlanUsuario> planOpt = planUsuarioRepository
                .findByUsuarioIdAndActivoTrueAndEliminadoFalse(usuarioId);

        if (planOpt.isEmpty()) {
            throw new RuntimeException(
                "No tienes un plan activo. Suscríbete a un plan para obtener una API Key."
            );
        }

        PlanUsuario plan = planOpt.get();

        if (plan.getFechaFinVigencia() != null &&
            plan.getFechaFinVigencia().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Tu plan ha vencido. Renueva tu suscripción.");
        }

        String apiKeyPlana = passwordHashUtil.generateApiKey();
        String apiKeyHash  = passwordHashUtil.hash(apiKeyPlana);

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime vence = ahora.plusYears(1);

        Optional<TokenUsuario> tokenExistente =
                tokenUsuarioRepository.findByUsuarioIdAndActivoTrue(usuarioId);

        TokenUsuario tokenUsuario;
        if (tokenExistente.isPresent()) {
            tokenUsuario = tokenExistente.get();
            log.info("Renovando API Key existente para usuarioId: {}", usuarioId);
        } else {
            tokenUsuario = new TokenUsuario();
            tokenUsuario.setUsuarioId(usuarioId);
            tokenUsuario.setUsuarioRegistro(usuarioId);
            log.info("Creando nueva API Key para usuarioId: {}", usuarioId);
        }

        tokenUsuario.setApiKey(apiKeyHash);
        tokenUsuario.setFechaInicioVigencia(ahora);
        tokenUsuario.setFechaFinVigencia(vence);
        tokenUsuario.setUsuarioModificacion(usuarioId);

        tokenUsuarioRepository.save(tokenUsuario);
        log.info("API Key guardada en BD para usuarioId: {}", usuarioId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("apiKey", apiKeyPlana);
        response.put("vigenciaInicio", ahora.toString());
        response.put("vigenciaFin", vence.toString());
        response.put("planId", plan.getPlanId());
        response.put("mensaje",
            "Guarda esta API Key en un lugar seguro. No se mostrará nuevamente.");

        return response;
    }

    public Map<String, Object> estadoApiKey(Integer usuarioId) {

        Optional<TokenUsuario> tokenOpt =
                tokenUsuarioRepository.findByUsuarioIdAndActivoTrue(usuarioId);

        boolean tieneApiKey = tokenOpt.isPresent() &&
                tokenOpt.get().getFechaFinVigencia() != null &&
                tokenOpt.get().getFechaFinVigencia().isAfter(LocalDateTime.now());

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("tieneApiKey", tieneApiKey);

        if (tieneApiKey) {
            TokenUsuario t = tokenOpt.get();
            response.put("vigenciaFin", t.getFechaFinVigencia().toString());
            response.put("mensaje", "Ya tienes una API Key activa.");
        } else {
            response.put("mensaje",
                "No tienes API Key activa. Genera una desde tu panel.");
        }

        return response;
    }
}
