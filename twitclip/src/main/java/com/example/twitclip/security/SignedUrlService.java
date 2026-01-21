package com.example.twitclip.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;

@Service
public class SignedUrlService {

    @Value("${app.download.secret}")
    private String secret;

    @PostConstruct
    public void validateSecret() {
        if (secret == null || secret.length() < 32) {
            throw new IllegalStateException("APP_DOWNLOAD_SECRET is missing or too weak");
        }
    }

    public String generateSignature(String filename, long expiresAt) {
        try {
            String payload = filename + ":" + expiresAt;

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));

            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);

        } catch (Exception e) {
            throw new RuntimeException("Failed to sign URL", e);
        }
    }

    public boolean isValid(String filename, long expiresAt, String signature) {
        if (Instant.now().toEpochMilli() > expiresAt) {
            return false;
        }
        return generateSignature(filename, expiresAt).equals(signature);
    }

}
