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

        // Buscar tokens activos y vigentes
        List<TokenUsuario> tokensActivos = tokenUsuarioRepository.findByActivoTrueAndFechaFinVigenciaBefore(LocalDateTime.now().plusYears(10));

        for (TokenUsuario tokenDb : tokensActivos) {
            if (passwordHashUtil.verify(tokenPlano, tokenDb.getTokenValue())) { // Buscar hash en TokenValue
                log.debug("Token validado correctamente para usuarioId: {}", tokenDb.getUsuarioId());
                return tokenDb.getUsuarioId();
            }
        }

        return null;
    }
}
