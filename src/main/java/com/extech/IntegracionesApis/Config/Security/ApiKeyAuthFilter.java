package com.extech.IntegracionesApis.Config.Security;

import com.extech.IntegracionesApis.Domain.Model.TokenUsuario;
import com.extech.IntegracionesApis.Repository.User.TokenUsuarioRepository;
import com.extech.IntegracionesApis.Util.Security.PasswordHashUtil;
import com.extech.IntegracionesApis.Util.Security.UserContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthFilter extends OncePerRequestFilter {

    private final TokenUsuarioRepository tokenUsuarioRepository;
    private final PasswordHashUtil passwordHashUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        
        // Skip validation for public endpoints
        if (requestURI.startsWith("/api/auth/login") || 
            requestURI.startsWith("/api/auth/register") ||
            requestURI.startsWith("/api/apis-externas/") ||
            requestURI.startsWith("/v3/api-docs") ||
            requestURI.startsWith("/swagger-ui") ||
            requestURI.startsWith("/actuator") ||
            requestURI.equals("/") ||
            requestURI.equals("/error")) {
            
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String tokenPlano = authHeader.substring(7);

            // Si parece JWT, no lo validamos como ApiKey (lo maneja JwtAuthenticationFilter)
            if (isJwtLike(tokenPlano)) {
                filterChain.doFilter(request, response);
                return;
            }

            Integer usuarioIdValidado = validarTokenYObtenerUsuarioId(tokenPlano);
            
            if (usuarioIdValidado != null) {
                // Establecer el UsuarioId en el contexto para uso posterior
                UserContext.setUsuarioId(usuarioIdValidado);
                
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken("user", null, Collections.emptyList());
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else {
                log.warn("Token inválido en request: {}", request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("{\"error\": \"Token inválido o expirado\"}");
                return;
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // Limpiar el contexto al finalizar la request
            UserContext.clear();
        }
    }

    private boolean isJwtLike(String token) {
        if (token == null) return false;
        int first = token.indexOf('.');
        if (first < 0) return false;
        int second = token.indexOf('.', first + 1);
        return second > first + 1 && second < token.length() - 1;
    }

    /**
     * Valida el token y retorna el UsuarioId si es válido
     * 
     * @param tokenPlano Token en texto plano del header Authorization
     * @return UsuarioId si el token es válido, null en caso contrario
     */
    private Integer validarTokenYObtenerUsuarioId(String tokenPlano) {
        if (tokenPlano == null || tokenPlano.isEmpty()) {
            return null;
        }

        log.info("🔍 Buscando token para validar: {} (longitud: {})", tokenPlano.substring(0, Math.min(10, tokenPlano.length())) + "...", tokenPlano.length());

        // Buscar tokens activos y vigentes
        List<TokenUsuario> tokensActivos = tokenUsuarioRepository.findByActivoTrueAndEliminadoFalseAndFechaFinVigenciaAfter(LocalDateTime.now());
        
        log.info("🔍 Tokens encontrados en BD: {}", tokensActivos.size());

        for (TokenUsuario tokenDb : tokensActivos) {
            String stored = tokenDb.getApiKey();
            if (stored == null || stored.isEmpty()) {
                log.warn("🔍 Token {} tiene apiKey nulo o vacío", tokenDb.getId());
                continue;
            }

            log.debug("🔍 Analizando token {} - stored length: {}", tokenDb.getId(), stored.length());

            // Formato esperado: "<hash>::<apiKeyEncriptadoBase64>"
            String hashPart = stored;
            int sepIdx = stored.indexOf("::");
            if (sepIdx > 0) {
                hashPart = stored.substring(0, sepIdx);
            }

            log.debug("🔍 Hash extraído: {}...", hashPart.substring(0, Math.min(20, hashPart.length())));

            if (passwordHashUtil.verify(tokenPlano, hashPart)) { // Verificar contra hash almacenado
                log.info("✅ Token validado correctamente para usuarioId: {}", tokenDb.getUsuarioId());
                return tokenDb.getUsuarioId();
            } else {
                log.debug("❌ Token no coincide para usuarioId: {}", tokenDb.getUsuarioId());
            }
        }

        log.warn("❌ No se encontró token válido para el token proporcionado");
        return null;
    }
}
