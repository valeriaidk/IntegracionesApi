package com.extech.IntegracionesApis.Service.Auth;



import com.extech.IntegracionesApis.Domain.Model.TokenUsuario;

import com.extech.IntegracionesApis.Domain.Model.Usuario;

import com.extech.IntegracionesApis.Config.Security.JwtProvider;

import com.extech.IntegracionesApis.Repository.Auth.AuthSpRepository;

import com.extech.IntegracionesApis.Repository.User.TokenUsuarioRepository;

import com.extech.IntegracionesApis.Repository.UsuarioRepository;

import com.extech.IntegracionesApis.Util.SecretEncryptionUtil;

import com.extech.IntegracionesApis.Util.Security.PasswordHashUtil;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;



import java.time.LocalDateTime;

import java.util.ArrayList;

import java.util.LinkedHashMap;

import java.util.List;

import java.util.Map;

import java.util.Optional;



@Service

@RequiredArgsConstructor

@Slf4j

public class AuthService {



    private static final String APIKEY_DELIMITER = "::";



    private final AuthSpRepository authSpRepository;

    private final TokenUsuarioRepository tokenUsuarioRepository;

    private final UsuarioRepository usuarioRepository;

    private final PasswordHashUtil passwordHashUtil;

    private final SecretEncryptionUtil secretEncryptionUtil;

    private final JwtProvider jwtProvider;



    private String buildStoredApiKey(String apiKeyHash, String apiKeyPlain) {

        String encrypted = secretEncryptionUtil.encrypt(apiKeyPlain);

        return apiKeyHash + APIKEY_DELIMITER + encrypted;

    }



    private String extractHash(String stored) {

        if (stored == null) return null;

        int idx = stored.indexOf(APIKEY_DELIMITER);

        return idx > 0 ? stored.substring(0, idx) : stored;

    }



    private String extractPlain(String stored) {

        if (stored == null) return null;

        int idx = stored.indexOf(APIKEY_DELIMITER);

        if (idx <= 0 || idx + APIKEY_DELIMITER.length() >= stored.length()) {

            return null;

        }

        String enc = stored.substring(idx + APIKEY_DELIMITER.length());

        return secretEncryptionUtil.decrypt(enc);

    }



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

        Integer planId = row.get("PlanId") != null ? ((Number) row.get("PlanId")).intValue() : null;
        String planNombre = row.get("PlanNombre") != null ? row.get("PlanNombre").toString() : "FREE";

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

        // API KEY — solo se genera la PRIMERA VEZ que el usuario inicia sesión
        // Si ya tiene una API Key activa → no se toca nunca
        Optional<TokenUsuario> tokenExistenteOpt =
            tokenUsuarioRepository.findByUsuarioIdAndActivoTrue(usuarioId);

        String apiKeyPlano = null;

        if (tokenExistenteOpt.isEmpty()) {
            apiKeyPlano = passwordHashUtil.generateApiKey();
            String apiKeyHash = passwordHashUtil.hash(apiKeyPlano);

            TokenUsuario tokenNuevo = new TokenUsuario();
            tokenNuevo.setUsuarioId(usuarioId);
            tokenNuevo.setApiKey(apiKeyHash);
            tokenNuevo.setFechaInicioVigencia(LocalDateTime.now());
            tokenNuevo.setFechaFinVigencia(LocalDateTime.now().plusYears(99));
            tokenNuevo.setUsuarioRegistro(usuarioId);
            tokenUsuarioRepository.save(tokenNuevo);

            log.info("✅ Primera vez: API Key generada para usuarioId: {}", usuarioId);
        } else {
            log.info("✅ Ya tiene API Key activa — no se regenera");
        }

        // JWT — se genera nuevo en cada login
        String jwt = jwtProvider.generateToken(
            emailBd,
            Map.of(
                "usuarioId", usuarioId,
                "email", emailBd,
                "plan", planNombre,
                "fullName", (nombre + " " + apellido).trim()
            )
        );

        Map<String, Object> planConfig = obtenerConfiguracionCompletaPlan(planId);

        Map<String, Object> usuarioMap = new LinkedHashMap<>();
        usuarioMap.put("usuarioId", usuarioId);
        usuarioMap.put("name", nombre);
        usuarioMap.put("fullName", (nombre + " " + apellido).trim());
        usuarioMap.put("email", emailBd);
        usuarioMap.put("plan", planNombre);
        usuarioMap.put("planConfig", planConfig);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("jwt", jwt);
        response.put("jwtTipo", "Bearer");
        response.put("usuario", usuarioMap);

        if (apiKeyPlano != null) {
            response.put("apiKey", apiKeyPlano);
            response.put("apiKeyMensaje",
                "Guarda esta API Key. No se mostrará nuevamente.");
        }

        log.info("🚀 Login exitoso: {} — Plan: {}", email, planNombre);

        return response;
    }



    /**

     * Genera manualmente una nueva ApiKey para un usuario existente.

     * Se usará desde el front con un botón "Generar token".

     */

    public Map<String, Object> regenerarApiKey(Integer usuarioId) {
        log.info("Solicitud de regeneración de ApiKey para usuarioId: {}", usuarioId);

        Optional<Usuario> usuarioOpt = usuarioRepository.findById(usuarioId);
        if (usuarioOpt.isEmpty()) {
            throw new RuntimeException("Usuario no encontrado");
        }

        Usuario usuario = usuarioOpt.get();
        if (Boolean.TRUE.equals(usuario.getEliminado()) || !Boolean.TRUE.equals(usuario.getActivo())) {
            throw new RuntimeException("Usuario inactivo o eliminado");
        }

        String apiKeyPlano = passwordHashUtil.generateApiKey();
        String apiKeyHash = passwordHashUtil.hash(apiKeyPlano);

        if (apiKeyHash == null || apiKeyHash.isEmpty()) {
            log.error("Error al generar nuevo ApiKey para usuarioId {}", usuarioId);
            throw new RuntimeException("Error al generar el token de autenticación");
        }

        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime vence = ahora.plusMonths(1);

        // Buscar token existente y actualizarlo (no crear uno nuevo)
        Optional<TokenUsuario> tokenExistenteOpt = tokenUsuarioRepository.findByUsuarioIdAndActivoTrue(usuarioId);
        
        TokenUsuario tokenUsuario;
        if (tokenExistenteOpt.isPresent()) {
            // Actualizar el token existente
            tokenUsuario = tokenExistenteOpt.get();
            log.info("Actualizando API Key existente para usuarioId {}", usuarioId);
        } else {
            // Crear nuevo token si no existe
            tokenUsuario = new TokenUsuario();
            tokenUsuario.setUsuarioId(usuarioId);
            tokenUsuario.setUsuarioRegistro(usuarioId);
            tokenUsuario.setActivo(true);
            log.info("Creando nueva API Key para usuarioId {}", usuarioId);
        }

        // Actualizar con la nueva API Key
        tokenUsuario.setApiKey(buildStoredApiKey(apiKeyHash, apiKeyPlano));
        tokenUsuario.setFechaInicioVigencia(ahora);
        tokenUsuario.setFechaFinVigencia(vence);
        tokenUsuario.setFechaModificacion(ahora);
        tokenUsuario.setUsuarioModificacion(usuarioId);

        tokenUsuarioRepository.save(tokenUsuario);
        log.info("API Key regenerada exitosamente para usuarioId {}", usuarioId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("token", apiKeyPlano);
        response.put("tipo", "ApiKey");
        response.put("usuarioId", usuarioId);

        return response;
    }



    /**

     * 📊 Obtiene la configuración completa del plan desde la base de datos

     */

    private Map<String, Object> obtenerConfiguracionCompletaPlan(Integer planId) {

        try {

            List<Map<String, Object>> planData = authSpRepository.obtenerConfiguracionPlan(planId);

            

            log.info("📊 Datos obtenidos del SP para planId {}: {} filas", planId, planData.size());

            planData.forEach(row -> log.info("   - Fila: {}", row));

            

            if (planData.isEmpty()) {

                log.warn("No se encontró configuración para el planId: {}", planId);

                return getConfiguracionPlanPorDefecto();

            }



            // Procesar los datos del plan

            Map<String, Object> config = new LinkedHashMap<>();

            

            // Datos básicos del plan

            Map<String, Object> planInfo = planData.get(0);

            config.put("nombre", planInfo.get("Nombre"));

            config.put("descripcion", planInfo.get("Descripcion"));

            config.put("precioMensual", planInfo.get("PrecioMensual"));

            

            // Procesar límites y funciones

            List<String> apisDisponibles = new ArrayList<>();

            List<String> caracteristicas = new ArrayList<>();

            List<String> restricciones = new ArrayList<>();

            String limitePeticiones = "1,000/mes"; // valor por defecto

            

            for (Map<String, Object> row : planData) {

                String funcionNombre = row.get("FuncionNombre") != null ? row.get("FuncionNombre").toString() : null;

                String tipoLimite = row.get("TipoLimite") != null ? row.get("TipoLimite").toString() : null;

                Integer limite = row.get("Limite") != null ? ((Number) row.get("Limite")).intValue() : null;

                

                log.debug("   Procesando fila - FuncionNombre: '{}', TipoLimite: '{}', Limite: {}", funcionNombre, tipoLimite, limite);

                

                if (funcionNombre != null && !funcionNombre.trim().isEmpty()) {

                    apisDisponibles.add(funcionNombre);

                    log.info("   ✅ API agregada: {}", funcionNombre);

                }

                

                if ("PETICIONES".equals(tipoLimite) && limite != null) {

                    limitePeticiones = limite + "/mes";

                    log.info("   📊 Límite de peticiones actualizado: {}", limitePeticiones);

                }

            }

            

            log.info("📋 APIs disponibles después de procesar: {}", apisDisponibles);

            

            // Agregar características según el plan

            if (planId != null) {

                switch (planId) {

                    case 1: // FREE

                        caracteristicas.add("Soporte comunidad");

                        caracteristicas.add("Documentación básica");

                        restricciones.add("Sin acceso a RUC");

                        restricciones.add("Sin SMS");

                        restricciones.add("Sin API personalizada");

                        break;

                    case 2: // ESTANDAR

                        caracteristicas.add("Soporte email");

                        caracteristicas.add("Documentación completa");

                        caracteristicas.add("API personalizada básica");

                        restricciones.add("SMS limitado a 100/mes");

                        restricciones.add("Sin analytics avanzado");

                        break;

                    case 3: // PREMIUM

                        caracteristicas.add("Soporte prioritario 24/7");

                        caracteristicas.add("API personalizada completa");

                        caracteristicas.add("Analytics avanzado");

                        caracteristicas.add("Webhooks personalizados");

                        restricciones.add("Sin restricciones");

                        break;

                }

            }

            

            config.put("limitePeticiones", limitePeticiones);

            config.put("apisDisponibles", apisDisponibles);

            config.put("caracteristicas", caracteristicas);

            config.put("restricciones", restricciones);

            

            log.info("Configuración del plan {} cargada correctamente", planId);

            return config;

            

        } catch (Exception e) {

            log.error("Error obteniendo configuración del plan {}: {}", planId, e.getMessage());

            return getConfiguracionPlanPorDefecto();

        }

    }



    /**

     * 📊 Configuración por defecto si no encuentra datos en la BD

     */

    private Map<String, Object> getConfiguracionPlanPorDefecto() {

        Map<String, Object> config = new LinkedHashMap<>();

        config.put("nombre", "FREE");

        config.put("descripcion", "Plan básico con funcionalidades limitadas");

        config.put("precioMensual", 0.00);

        config.put("limitePeticiones", "1,000/mes");

        config.put("apisDisponibles", List.of("Consulta DNI"));

        config.put("caracteristicas", List.of("Soporte comunidad", "Documentación básica"));

        config.put("restricciones", List.of("Sin acceso a RUC", "Sin SMS", "Sin API personalizada"));

        return config;

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

