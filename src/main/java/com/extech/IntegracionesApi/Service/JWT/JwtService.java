package com.extech.IntegracionesApis.Service.JWT;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class JwtService {
    
    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:86400000}") // 24 horas en milisegundos
    private Long jwtExpiration;
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    public String generateToken(String username, String password, String planType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("password", password);
        claims.put("planType", planType);
        claims.put("tokenType", "auth");
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    public String generateApiToken(String username, String planType) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", username);
        claims.put("planType", planType);
        claims.put("tokenType", "api");
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    
    public String extractPlanType(String token) {
        return extractAllClaims(token).get("planType", String.class);
    }
    
    public String extractTokenype(String token) {
        return extractAllClaims(token).get("tokenType", String.class);
    }
    
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
    
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public Boolean validateToken(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            return (tokenUsername.equals(username) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Boolean validateApiToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            String tokenType = claims.get("tokenType", String.class);
            return "api".equals(tokenType) && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
    
    public Map<String, Object> getTokenInfo(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Map<String, Object> info = new HashMap<>();
            info.put("username", claims.getSubject());
            info.put("planType", claims.get("planType"));
            info.put("tokenType", claims.get("tokenType"));
            info.put("issuedAt", claims.getIssuedAt());
            info.put("expiration", claims.getExpiration());
            info.put("isValid", !isTokenExpired(token));
            return info;
        } catch (JwtException | IllegalArgumentException e) {
            Map<String, Object> info = new HashMap<>();
            info.put("error", "Token inválido: " + e.getMessage());
            info.put("isValid", false);
            return info;
        }
    }
}
