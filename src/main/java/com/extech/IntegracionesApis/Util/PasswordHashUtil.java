package com.extech.IntegracionesApis.Util.Security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Utilitario para hashear contraseñas y API Keys usando BCrypt.
 * BCrypt es one-way (no se puede desencriptar), ideal para:
 * - Passwords de usuarios
 * - API Keys (solo verificación, no recuperación)
 */
@Component
public class PasswordHashUtil {

    private final BCryptPasswordEncoder encoder;

    public PasswordHashUtil() {
        this.encoder = new BCryptPasswordEncoder();
    }

    public String hash(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return null;
        }
        return encoder.encode(plainText);
    }

    public boolean verify(String plainText, String hashedText) {
        if (plainText == null || hashedText == null) {
            return false;
        }
        return encoder.matches(plainText, hashedText);
    }

    public String generateApiKey() {
        return java.util.UUID.randomUUID().toString().replace("-", "");
    }
}
