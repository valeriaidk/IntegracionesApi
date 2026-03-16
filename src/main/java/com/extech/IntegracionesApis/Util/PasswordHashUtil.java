package com.extech.IntegracionesApis.Util.Security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitario para hashear contraseñas y API Keys usando BCrypt.
 * BCrypt es one-way (no se puede desencriptar), ideal para:
 * - Passwords de usuarios
 * - API Keys (solo verificación, no recuperación)
 */
@Component
public class PasswordHashUtil {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String hash(String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        return encoder.encode(value);
    }

    public boolean verify(String plainValue, String hashedValue) {
        if (plainValue == null || hashedValue == null || hashedValue.isBlank()) {
            return false;
        }
        return encoder.matches(plainValue, hashedValue);
    }

    public String generateApiKey() {
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
