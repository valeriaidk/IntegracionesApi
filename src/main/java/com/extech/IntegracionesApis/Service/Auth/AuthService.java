package com.extech.IntegracionesApis.Service.Auth;

import com.extech.IntegracionesApis.Domain.Model.TokenUsuario;
import com.extech.IntegracionesApis.Domain.Model.Usuario;
import com.extech.IntegracionesApis.Repository.Auth.AuthSpRepository;
import com.extech.IntegracionesApis.Repository.User.TokenUsuarioRepository;
import com.extech.IntegracionesApis.Repository.UsuarioRepository;
import com.extech.IntegracionesApis.Util.Security.PasswordHashUtil;
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
public class AuthService {

    private final AuthSpRepository authSpRepository;
    private final TokenUsuarioRepository tokenUsuarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordHashUtil passwordHashUtil;

    public Map<String, Object> autenticar(String email, String password) {
        log.info("Intentando autenticar usuario: {}", email);

        List<Map<String, Object>> rows = authSpRepository.validarAcceso(email);

        if (rows == null || rows.isEmpty()) {
            log.warn("Usuario no encontrado: {}", email);
            throw new RuntimeException("Credenciales inválidas");
        }

        Map<String, Object> row = rows.get(0);

        Integer usuarioId = row.get("UsuarioId") != null ? ((Number) row.get("UsuarioId")).intValue() : null;
        String nombre = row.get("Nombre") != null ? row.get("Nombre").toString() : "";
        String apellido = row.get("Apellido") != null ? row.get("Apellido").toString() : "";
        String emailBd = row.get("Email") != null ? row.get("Email").toString() : "";
        String passwordHash = row.get("PasswordHash") != null ? row.get("PasswordHash").toString() : null;

        // 🏷️ Obtener plan real del usuario desde la base de datos
        Integer planId = row.get("PlanId") != null ? ((Number) row.get("PlanId")).intValue() : null;
        String planNombre = row.get("PlanNombre") != null ? row.get("PlanNombre").toString() : "FREE";
        
        // Si no hay PlanNombre pero hay PlanId, usar un valor por defecto según el ID
        if (planNombre == null || planNombre.trim().isEmpty()) {
            if (planId != null) {
                switch (planId) {
                    case 1: planNombre = "FREE"; break;
                    case 2: planNombre = "ESTANDAR"; break;
                    case 3: planNombre = "PREMIUM"; break;
                    default: planNombre = "FREE"; break;
                }
            } else {
                planNombre = "FREE";
            }
        }

        Boolean activo = row.get("Activo") != null && Boolean.parseBoolean(row.get("Activo").toString());
        Boolean eliminado = row.get("Eliminado") != null && Boolean.parseBoolean(row.get("Eliminado").toString());

        if (!Boolean.TRUE.equals(activo) || Boolean.TRUE.equals(eliminado)) {
            log.warn("Usuario inactivo o eliminado: {}", email);
            throw new RuntimeException("Usuario inactivo");
        }

        boolean passwordValido = passwordHashUtil.verify(password, passwordHash);

        if (!passwordValido) {
            log.warn("Contraseña incorrecta para usuario: {}", email);
            throw new RuntimeException("Credenciales inválidas");
        }

        String apiKeyPlano = passwordHashUtil.generateApiKey();
        String apiKeyHash = passwordHashUtil.hash(apiKeyPlano);

        if (apiKeyHash == null || apiKeyHash.isEmpty()) {
            log.error("Error: No se pudo generar el hash del token. apiKeyPlano generado: {}", apiKeyPlano);
            throw new RuntimeException("Error al generar el token de autenticación");
        }

        log.debug("Token generado para usuario {} - apiKeyPlano length: {}, apiKeyHash length: {}", 
                  email, apiKeyPlano.length(), apiKeyHash.length());

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime vence = ahora.plusMonths(1);

        // Verificar si el usuario ya tiene un token activo
        Optional<TokenUsuario> tokenExistenteOpt = tokenUsuarioRepository.findByUsuarioIdAndActivoTrue(usuarioId);
        
        TokenUsuario tokenUsuario;
        if (tokenExistenteOpt.isPresent()) {
            // Actualizar token existente
            tokenUsuario = tokenExistenteOpt.get();
            tokenUsuario.setTokenValue(apiKeyHash); // Guardar hash en TokenValue
            tokenUsuario.setFechaInicioVigencia(ahora);
            tokenUsuario.setFechaFinVigencia(vence);
            tokenUsuario.setFechaModificacion(ahora);
            tokenUsuario.setUsuarioModificacion(usuarioId);
            log.info("Actualizando token existente para usuario {}", email);
        } else {
            // Crear nuevo token
            tokenUsuario = new TokenUsuario();
            tokenUsuario.setUsuarioId(usuarioId);
            tokenUsuario.setTokenValue(apiKeyHash); // Guardar hash en TokenValue
            tokenUsuario.setFechaInicioVigencia(ahora);
            tokenUsuario.setFechaFinVigencia(vence);
            tokenUsuario.setUsuarioRegistro(usuarioId);
            log.info("Creando nuevo token para usuario {}", email);
        }
        
        tokenUsuarioRepository.save(tokenUsuario);
        log.info("Token guardado en BD para usuario {} con ID de token: {}", email, tokenUsuario.getId());

        log.info("Usuario autenticado exitosamente: {} (Plan: {})", email, planNombre);

        Map<String, Object> usuarioMap = new LinkedHashMap<>();
        usuarioMap.put("usuarioId", usuarioId);
        usuarioMap.put("name", nombre);
        usuarioMap.put("fullName", (nombre + " " + apellido).trim());
        usuarioMap.put("email", emailBd);
        usuarioMap.put("plan", planNombre); // 🏷️ Usar plan real de la BD

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", apiKeyPlano);
        response.put("tipo", "ApiKey");
        response.put("usuario", usuarioMap);

        return response;
    }

    public Map<String, Object> actualizarPassword(String email, String nuevaPassword) {
        log.info("Solicitud de cambio de contraseña para: {}", email);

        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmailAndActivoTrue(email);

        if (usuarioOpt.isEmpty()) {
            log.warn("Usuario no encontrado para cambio de contraseña: {}", email);
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();

        if (Boolean.TRUE.equals(usuario.getEliminado())) {
            log.warn("Usuario eliminado intentando cambiar contraseña: {}", email);
            throw new RuntimeException("Usuario eliminado");
        }

        String nuevoHash = passwordHashUtil.hash(nuevaPassword);

        usuario.setPasswordHash(nuevoHash);
        usuario.setFechaModificacion(LocalDateTime.now());

        usuarioRepository.save(usuario);

        log.info("Contraseña actualizada exitosamente para: {}", email);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Contraseña actualizada correctamente");
        response.put("email", email);

        return response;
    }

    public Usuario createUser(String username, String password, String fullName, String email, String planType) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new RuntimeException("El email ya está registrado");
        }

        Usuario nuevoUsuario = new Usuario();
        String[] nombres = fullName.split(" ", 2);
        nuevoUsuario.setNombre(nombres.length > 0 ? nombres[0] : fullName);
        nuevoUsuario.setApellido(nombres.length > 1 ? nombres[1] : "");
        nuevoUsuario.setEmail(email);
        nuevoUsuario.setPasswordHash(passwordHashUtil.hash(password));
        nuevoUsuario.setActivo(true);

        Usuario savedUsuario = usuarioRepository.save(nuevoUsuario);
        log.info("Usuario creado en base de datos: {}", savedUsuario.getEmail());

        return savedUsuario;
    }

    public Map<String, Object> registrarUsuario(String nombre, String apellido, String email, String password, Integer planId, String telefono, String razonSocial, String ruc, Boolean activo, Boolean eliminado, Integer usuarioAccion) {
        log.info("Registrando nuevo usuario: {}", email);

        // Hashear contraseña
        String passwordHash = passwordHashUtil.hash(password);

        // Llamar al SP con usuarioId = null para crear nuevo usuario
        List<Map<String, Object>> resultado = authSpRepository.guardarOActualizarUsuario(
                null, // usuarioId null para crear
                nombre,
                apellido,
                email,
                passwordHash,
                planId, // puede ser null, el SP asignará FREE
                telefono,
                razonSocial,
                ruc,
                activo != null ? activo : true, // por defecto activo = true
                eliminado != null ? eliminado : false, // por defecto eliminado = false
                usuarioAccion
        );

        if (resultado.isEmpty()) {
            throw new RuntimeException("Error al registrar usuario");
        }

        Map<String, Object> spResult = resultado.get(0);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("usuarioId", spResult.get("UsuarioId"));
        response.put("planId", spResult.get("PlanId"));
        response.put("accion", "CREADO");
        response.put("message", "Usuario registrado exitosamente");
        response.put("email", email);

        log.info("Usuario registrado exitosamente: {} (ID: {})", email, spResult.get("UsuarioId"));
        return response;
    }

    public Map<String, Object> actualizarUsuario(Integer usuarioId, String nombre, String apellido, String email, String password, Integer planId, String telefono, String razonSocial, String ruc, Boolean activo, Boolean eliminado, Integer usuarioAccion) {
        log.info("Actualizando usuario ID: {}", usuarioId);

        String passwordHash = null;
        if (password != null && !password.trim().isEmpty()) {
            passwordHash = passwordHashUtil.hash(password);
        }

        // Llamar al SP con todos los parámetros
        List<Map<String, Object>> resultado = authSpRepository.guardarOActualizarUsuario(
                usuarioId,
                nombre,
                apellido,
                email,
                passwordHash, // null si no se quiere cambiar password
                planId, // null si no se quiere cambiar plan
                telefono,
                razonSocial,
                ruc,
                activo,
                eliminado,
                usuarioAccion
        );

        if (resultado.isEmpty()) {
            throw new RuntimeException("Error al actualizar usuario");
        }

        Map<String, Object> spResult = resultado.get(0);
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("usuarioId", spResult.get("UsuarioId"));
        response.put("planId", spResult.get("PlanId"));
        response.put("accion", "ACTUALIZADO");
        response.put("message", "Usuario actualizado exitosamente");
        response.put("email", email);

        log.info("Usuario actualizado exitosamente: {} (ID: {})", email, usuarioId);
        return response;
    }
}
