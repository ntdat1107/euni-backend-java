package com.euni.backend.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Slf4j
@Service
public class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128; // bits
    private static final int GCM_IV_LENGTH = 12;   // bytes

    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public CryptoService(@Value("${app.crypto.secret-key:eUniDefaultSecretKeyForEncryption2026AES}") String rawKey) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = digest.digest(rawKey.getBytes(StandardCharsets.UTF_8));
            this.secretKey = new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            log.error("Failed to initialize CryptoService secret key", e);
            throw new IllegalStateException("Crypto initialization failure", e);
        }
    }

    public record EncryptedResult(String ciphertext, String iv) {}

    public EncryptedResult encrypt(String plainText) {
        if (plainText == null || plainText.trim().isEmpty()) {
            throw new IllegalArgumentException("Plaintext cannot be empty");
        }
        try {
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);

            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            String encodedCipherText = Base64.getEncoder().encodeToString(cipherText);
            String encodedIv = Base64.getEncoder().encodeToString(iv);

            return new EncryptedResult(encodedCipherText, encodedIv);
        } catch (Exception e) {
            log.error("Encryption error: {}", e.getMessage());
            throw new RuntimeException("Encryption operation failed", e);
        }
    }

    public String decrypt(String ciphertext, String iv) {
        if (ciphertext == null || iv == null) {
            throw new IllegalArgumentException("Ciphertext and IV cannot be null");
        }
        try {
            byte[] decodedIv = Base64.getDecoder().decode(iv);
            byte[] decodedCipherText = Base64.getDecoder().decode(ciphertext);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, decodedIv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);

            byte[] plainTextBytes = cipher.doFinal(decodedCipherText);
            return new String(plainTextBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption error: {}", e.getMessage());
            throw new RuntimeException("Decryption operation failed", e);
        }
    }

    public static String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return "";
        }
        String trimmed = apiKey.trim();
        if (trimmed.length() <= 8) {
            return "********";
        }
        if (trimmed.startsWith("sk-") && trimmed.length() > 10) {
            return "sk-..." + trimmed.substring(trimmed.length() - 4);
        }
        return trimmed.substring(0, 4) + "..." + trimmed.substring(trimmed.length() - 4);
    }
}
