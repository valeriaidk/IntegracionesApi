package com.extech.IntegracionesApis.Util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitario para cifrar/descifrar tokens de APIs externas usando AES-GCM.
 * Los tokens se cifran antes de guardar en BD y se descifran solo en memoria.
 */
@Component
@Slf4j
public class SecretEncryptionUtil {

    @Value("${app.encryption.key}")
    private String encryptionKey;

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 16;

    /**
     * Cifra un texto plano usando AES-GCM
     * @param plainText Texto a cifrar (ej: token real)
     * @return Texto cifrado en Base64
     */
    public String encrypt(String plainText) {
        try {
            if (plainText == null || plainText.isEmpty()) {
                return plainText;
            }

            // Convertir clave a bytes
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length != 32) {
                throw new IllegalArgumentException("La clave de cifrado debe tener exactamente 32 caracteres");
            }

            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // Generar IV aleatorio
            byte[] iv = new byte[GCM_IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combinar IV + ciphertext y codificar en Base64
            byte[] encrypted = new byte[iv.length + cipherText.length];
            System.arraycopy(iv, 0, encrypted, 0, iv.length);
            System.arraycopy(cipherText, 0, encrypted, iv.length, cipherText.length);

            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            log.error("Error al cifrar texto: {}", e.getMessage(), e);
            throw new RuntimeException("Error al cifrar texto", e);
        }
    }

    /**
     * Descifra un texto cifrado usando AES-GCM
     * @param encryptedText Texto cifrado en Base64
     * @return Texto plano original
     */
    public String decrypt(String encryptedText) {
        try {
            if (encryptedText == null || encryptedText.isEmpty()) {
                return encryptedText;
            }

            // Convertir clave a bytes
            byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length != 32) {
                throw new IllegalArgumentException("La clave de cifrado debe tener exactamente 32 caracteres");
            }

            SecretKeySpec secretKey = new SecretKeySpec(keyBytes, ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // Decodificar Base64 y extraer IV
            byte[] encrypted = Base64.getDecoder().decode(encryptedText);
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] cipherText = new byte[encrypted.length - GCM_IV_LENGTH];

            System.arraycopy(encrypted, 0, iv, 0, iv.length);
            System.arraycopy(encrypted, iv.length, cipherText, 0, cipherText.length);

            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] decryptedText = cipher.doFinal(cipherText);
            return new String(decryptedText, StandardCharsets.UTF_8);

        } catch (Exception e) {
            log.error("Error al descifrar texto: {}", e.getMessage(), e);
            throw new RuntimeException("Error al descifrar texto", e);
        }
    }

    /**
     * Verifica si un texto parece estar cifrado (Base64 válido)
     * @param text Texto a verificar
     * @return true si parece cifrado, false si parece texto plano
     */
    public boolean isEncrypted(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(text);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
