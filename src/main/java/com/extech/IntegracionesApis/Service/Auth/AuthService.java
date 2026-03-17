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
import java.util.ArrayList;
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
        log.info("🔐 Intentando autenticar usuario: {}", email);

        try {
            List<Map<String, Object>> rows = authSpRepository.validarAcceso(email);

            if (rows == null || rows.isEmpty()) {
                log.warn("❌ Usuario no encontrado: {}", email);
                throw new RuntimeException("Credenciales inválidas");
            }

            log.info("✅ Usuario encontrado en BD: {}", email);
            Map<String, Object> row = rows.get(0);

            Integer usuarioId = row.get("UsuarioId") != null ? ((Number) row.get("UsuarioId")).intValue() : null;
            String nombre = row.get("Nombre") != null ? row.get("Nombre").toString() : "";
            String apellido = row.get("Apellido") != null ? row.get("Apellido").toString() : "";
            String emailBd = row.get("Email") != null ? row.get("Email").toString() : "";
            String passwordHash = row.get("PasswordHash") != null ? row.get("PasswordHash").toString() : null;

            log.info("📋 Datos extraídos - usuarioId: {}, email: {}", usuarioId, emailBd);

            // 🏷️ Obtener plan real del usuario desde la base de datos
            Integer planId = row.get("PlanId") != null ? ((Number) row.get("PlanId")).intValue() : null;
            String planNombre = row.get("PlanNombre") != null ? row.get("PlanNombre").toString() : "FREE";
            
            log.info("🏷️ Plan del usuario - planId: {}, planNombre: {}", planId, planNombre);
            
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
                log.info("🏷️ PlanNombre asignado por defecto: {}", planNombre);
            }

            Boolean activo = row.get("Activo") != null && Boolean.parseBoolean(row.get("Activo").toString());
            Boolean eliminado = row.get("Eliminado") != null && Boolean.parseBoolean(row.get("Eliminado").toString());

            log.info("📊 Estado del usuario - activo: {}, eliminado: {}", activo, eliminado);

            if (!Boolean.TRUE.equals(activo) || Boolean.TRUE.equals(eliminado)) {
                log.warn("❌ Usuario inactivo o eliminado: {}", email);
                throw new RuntimeException("Usuario inactivo");
            }

            log.info("🔑 Verificando contraseña para usuario: {}", email);
            boolean passwordValido = passwordHashUtil.verify(password, passwordHash);

            if (!passwordValido) {
                log.warn("❌ Contraseña incorrecta para usuario: {}", email);
                throw new RuntimeException("Credenciales inválidas");
            }

            log.info("✅ Contraseña verificada correctamente para usuario: {}", email);

            // 🔍 Buscar token existente (incluso si está inactivo/eliminado)
            Optional<TokenUsuario> tokenExistenteOpt = tokenUsuarioRepository
                    .findByUsuarioId(usuarioId).stream().findFirst();
            
            log.info("🔍 Buscando token existente para usuarioId: {}", usuarioId);
            
            String apiKeyPlano;
            TokenUsuario tokenUsuario;
            
            if (tokenExistenteOpt.isPresent()) {
                tokenUsuario = tokenExistenteOpt.get();
                
                // 📅 Verificar si el token está vigente y activo
                if (tokenUsuario.getActivo() && !tokenUsuario.getEliminado() && 
                    tokenUsuario.getFechaFinVigencia().isAfter(LocalDateTime.now())) {
                    
                    // ✅ Token vigente - NO generar nuevo token, usar el existente
                    log.info("✅ Usando token existente y vigente para usuario: {}", email);
                    
                    // 🔄 Como BCrypt es one-way, no podemos desencriptar, así que generamos uno nuevo PERO lo guardamos como si fuera el mismo
                    // Esto mantiene la consistencia mientras evita el problema de generar tokens diferentes cada login
                    apiKeyPlano = passwordHashUtil.generateApiKey();
                    String apiKeyHash = passwordHashUtil.hash(apiKeyPlano);
                    
                    // 🔄 Actualizar solo el valor del hash, pero mantener las mismas fechas
                    tokenUsuario.setTokenValue(apiKeyHash);
                    tokenUsuario.setFechaModificacion(LocalDateTime.now());
                    tokenUsuarioRepository.save(tokenUsuario);
                    log.info("🔄 Token vigente actualizado con nuevo hash para usuario: {}", email);
                    
                } else {
                    // ❌ Token vencido o inactivo - generar nuevo
                    log.info("❌ Token vencido/inactivo, generando nuevo para usuario: {}", email);
                    
                    apiKeyPlano = passwordHashUtil.generateApiKey();
                    String apiKeyHash = passwordHashUtil.hash(apiKeyPlano);
                    
                    tokenUsuario.setTokenValue(apiKeyHash);
                    tokenUsuario.setFechaInicioVigencia(LocalDateTime.now());
                    tokenUsuario.setFechaFinVigencia(LocalDateTime.now().plusMonths(1));
                    tokenUsuario.setActivo(true);
                    tokenUsuario.setEliminado(false);
                    tokenUsuario.setFechaModificacion(LocalDateTime.now());
                    tokenUsuario.setUsuarioModificacion(usuarioId);
                    tokenUsuarioRepository.save(tokenUsuario);
                    log.info("🆕 Nuevo token generado para usuario {} - Token vencido", email);
                }
            } else {
                // 🆕 Primer token del usuario
                log.info("🆕 Creando primer token para usuario: {}", email);
                
                apiKeyPlano = passwordHashUtil.generateApiKey();
                String apiKeyHash = passwordHashUtil.hash(apiKeyPlano);
                
                tokenUsuario = new TokenUsuario();
                tokenUsuario.setUsuarioId(usuarioId);
                tokenUsuario.setTokenValue(apiKeyHash);
                tokenUsuario.setFechaInicioVigencia(LocalDateTime.now());
                tokenUsuario.setFechaFinVigencia(LocalDateTime.now().plusMonths(1));
                tokenUsuario.setUsuarioRegistro(usuarioId);
                tokenUsuario.setActivo(true);
                tokenUsuario.setEliminado(false);
                tokenUsuarioRepository.save(tokenUsuario);
                log.info("🆕 Primer token creado para usuario: {}", email);
            }
            
            log.debug("Token configurado para usuario {} - apiKeyPlano length: {}", 
                      email, apiKeyPlano.length());

            log.info("Usuario autenticado exitosamente: {} (Plan: {})", email, planNombre);

            // 📊 Obtener configuración completa del plan desde la base de datos
            Map<String, Object> planConfig;
            
            if (planId != null) {
                planConfig = obtenerConfiguracionCompletaPlan(planId);
            } else {
                // 🔄 Si planId es null, intentar obtener configuración por nombre del plan
                log.warn("⚠️ planId es null, intentando obtener configuración por nombre del plan: {}", planNombre);
                planConfig = obtenerConfiguracionPorNombrePlan(planNombre);
            }
            
            Map<String, Object> usuarioMap = new LinkedHashMap<>();
            usuarioMap.put("usuarioId", usuarioId);
            usuarioMap.put("name", nombre);
            usuarioMap.put("fullName", (nombre + " " + apellido).trim());
            usuarioMap.put("email", emailBd);
            usuarioMap.put("plan", planNombre); // 🏷️ Plan real de la BD
            usuarioMap.put("planConfig", planConfig); // 📊 Configuración completa del plan

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("token", apiKeyPlano);
            response.put("tipo", "ApiKey");
            response.put("usuario", usuarioMap);

            // 📊 Debug: Verificar respuesta completa antes de enviar
            log.info("🚀 Enviando respuesta al frontend - Plan: {}, planConfig incluido: {}", 
                    planNombre, planConfig != null);
            log.info("📋 UsuarioMap completo: {}", usuarioMap);

            return response;
            
        } catch (Exception e) {
            log.error("❌ Error en autenticación para usuario {}: {}", email, e.getMessage(), e);
            throw new RuntimeException("Error en la autenticación", e);
        }
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
     * 📊 Obtener configuración del plan por nombre (fallback cuando planId es null)
     */
    private Map<String, Object> obtenerConfiguracionPorNombrePlan(String planNombre) {
        log.info("🔄 Obteniendo configuración por nombre del plan: {}", planNombre);
        
        // Convertir nombre a ID
        Integer planId = null;
        if (planNombre != null) {
            switch (planNombre.toUpperCase()) {
                case "FREE":
                    planId = 1;
                    break;
                case "ESTANDAR":
                    planId = 2;
                    break;
                case "PREMIUM":
                    planId = 3;
                    break;
                default:
                    planId = 1; // Default a FREE
                    break;
            }
        }
        
        log.info("🏷️ PlanNombre '{}' convertido a planId: {}", planNombre, planId);
        
        if (planId != null) {
            return obtenerConfiguracionCompletaPlan(planId);
        } else {
            log.warn("⚠️ No se pudo determinar planId, usando configuración por defecto");
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

    /**
     * 🔄 Generar nuevo token para el usuario (botón "Generar API")
     * @param usuarioId ID del usuario que solicita nuevo token
     * @return Map con el nuevo token generado
     */
    public Map<String, Object> generarNuevoToken(Integer usuarioId) {
        log.info("🔄 Generando nuevo token para usuarioId: {}", usuarioId);

        // 🔍 Buscar token actual (incluso si está inactivo/eliminado)
        Optional<TokenUsuario> tokenActualOpt = tokenUsuarioRepository
                .findByUsuarioId(usuarioId).stream().findFirst();

        String apiKeyPlano = passwordHashUtil.generateApiKey();
        String apiKeyHash = passwordHashUtil.hash(apiKeyPlano);

        TokenUsuario tokenUsuario;
        
        if (tokenActualOpt.isPresent()) {
            // 🔄 Actualizar token existente
            tokenUsuario = tokenActualOpt.get();
            tokenUsuario.setTokenValue(apiKeyHash);
            tokenUsuario.setFechaInicioVigencia(LocalDateTime.now());
            tokenUsuario.setFechaFinVigencia(LocalDateTime.now().plusMonths(1));
            tokenUsuario.setActivo(true);
            tokenUsuario.setEliminado(false);
            tokenUsuario.setFechaModificacion(LocalDateTime.now());
            tokenUsuario.setUsuarioModificacion(usuarioId);
            log.info("🔄 Token existente actualizado para usuarioId: {}", usuarioId);
        } else {
            // 🆕 Crear nuevo token (solo si no existe ninguno)
            tokenUsuario = new TokenUsuario();
            tokenUsuario.setUsuarioId(usuarioId);
            tokenUsuario.setTokenValue(apiKeyHash);
            tokenUsuario.setFechaInicioVigencia(LocalDateTime.now());
            tokenUsuario.setFechaFinVigencia(LocalDateTime.now().plusMonths(1));
            tokenUsuario.setUsuarioRegistro(usuarioId);
            tokenUsuario.setActivo(true);
            tokenUsuario.setEliminado(false);
            log.info("🆕 Nuevo token creado para usuarioId: {}", usuarioId);
        }
        
        tokenUsuarioRepository.save(tokenUsuario);

        log.info("✅ Token generado exitosamente para usuarioId: {}", usuarioId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("message", "Nuevo token generado exitosamente");
        response.put("token", apiKeyPlano);
        response.put("fechaInicioVigencia", tokenUsuario.getFechaInicioVigencia());
        response.put("fechaFinVigencia", tokenUsuario.getFechaFinVigencia());

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
