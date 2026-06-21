package com.simon.workspace.auth.session;

import com.simon.workspace.auth.model.AuthUser;
import com.simon.workspace.auth.model.CurrentUser;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class TokenStore {

    private static final Duration TOKEN_TTL = Duration.ofHours(12);
    private static final int TOKEN_BYTES = 32;

    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, TokenSession> sessions = new ConcurrentHashMap<>();

    public String create(AuthUser authUser) {
        String token = nextToken();
        sessions.put(token, new TokenSession(authUser.toCurrentUser(), Instant.now().plus(TOKEN_TTL)));
        return token;
    }

    public Optional<CurrentUser> find(String token) {
        if (token == null) {
            return Optional.empty();
        }

        TokenSession session = sessions.get(token);
        if (session == null) {
            return Optional.empty();
        }

        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(token);
            return Optional.empty();
        }

        return Optional.of(session.user());
    }

    public void remove(String token) {
        if (token != null) {
            sessions.remove(token);
        }
    }

    private String nextToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private record TokenSession(CurrentUser user, Instant expiresAt) {
    }
}
