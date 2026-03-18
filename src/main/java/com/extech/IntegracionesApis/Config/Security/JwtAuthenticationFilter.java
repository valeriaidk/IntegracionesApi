package com.extech.IntegracionesApis.Config.Security;

import com.extech.IntegracionesApis.Util.Security.UserContext;
import io.jsonwebtoken.Claims;
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
import java.util.Collections;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        try {
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                // Detectar JWT por estructura: header.payload.signature (2 puntos)
                if (isJwtLike(token)) {
                    Claims claims = jwtProvider.parseClaims(token);

                    Integer usuarioId = null;
                    Object uid = claims.get("usuarioId");
                    if (uid instanceof Number) {
                        usuarioId = ((Number) uid).intValue();
                    } else if (uid != null) {
                        try {
                            usuarioId = Integer.parseInt(uid.toString());
                        } catch (Exception ignored) {
                        }
                    }

                    if (usuarioId != null) {
                        UserContext.setUsuarioId(usuarioId);
                    }

                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(claims.getSubject(), null, Collections.emptyList());
                    SecurityContextHolder.getContext().setAuthentication(auth);
                }
            }

            filterChain.doFilter(request, response);
        } catch (Exception e) {
            log.warn("JWT inválido en {}: {}", request.getRequestURI(), e.getMessage());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"error\": \"JWT inválido o expirado\"}");
        } finally {
            // Limpiar contexto por request
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
}
