package com.extech.IntegracionesApis.Util.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class TokenEncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String SECRET_KEY = "A7b3K9mX2pQ8vR4nT6wY1zF5hG9jL3pQ";

    public String encrypt(String tokenPlano) throws Exception {
        if (tokenPlano == null || tokenPlano.isEmpty()) {
            return null;
        }
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encrypted = cipher.doFinal(tokenPlano.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    public String decrypt(String tokenEncriptado) throws Exception {
        if (tokenEncriptado == null || tokenEncriptado.isEmpty()) {
            return null;
        }
        SecretKeySpec key = new SecretKeySpec(SECRET_KEY.getBytes(), ALGORITHM);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decoded = Base64.getDecoder().decode(tokenEncriptado);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted);
    }
}
