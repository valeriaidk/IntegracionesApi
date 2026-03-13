package com.extech.IntegracionesApis.Util.Security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TokenHashUtil {

    private final BCryptPasswordEncoder encoder;

    public TokenHashUtil() {
        this.encoder = new BCryptPasswordEncoder();
    }

    public String hashearToken(String tokenPlano) {
        if (tokenPlano == null || tokenPlano.isEmpty()) {
            return null;
        }
        return encoder.encode(tokenPlano);
    }

    public boolean verificarToken(String tokenPlano, String tokenHasheado) {
        if (tokenPlano == null || tokenHasheado == null) {
            return false;
        }
        return encoder.matches(tokenPlano, tokenHasheado);
    }

    public String obtenerTokenParaApiExterna(String tokenEncriptado, String claveMaestra) {
        return null;
    }
}
