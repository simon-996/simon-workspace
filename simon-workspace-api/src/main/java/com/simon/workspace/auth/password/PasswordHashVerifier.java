package com.simon.workspace.auth.password;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
public class PasswordHashVerifier {

    private static final String SHA256_PREFIX = "sha256:";

    public boolean matches(String rawPassword, String storedHash) {
        if (!StringUtils.hasText(rawPassword) || !StringUtils.hasText(storedHash)) {
            return false;
        }

        if (storedHash.startsWith(SHA256_PREFIX)) {
            String expected = storedHash.substring(SHA256_PREFIX.length());
            return constantTimeEquals(expected, sha256(rawPassword));
        }

        return false;
    }

    public String sha256(String rawPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8)
        );
    }
}
