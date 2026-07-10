package com.daisy.health.common;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {
    private static final String SECRET = "daisy-health-local-dev-secret-please-change-32";

    @Test
    void tokenIsValidForEightHours() {
        JwtService jwtService = new JwtService(SECRET, configuredExpirationMs());
        String token = jwtService.createToken(10001L, "elderly", "13800010001");
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();

        Date issuedAt = claims.getIssuedAt();
        Date expiresAt = claims.getExpiration();
        assertThat(expiresAt.getTime() - issuedAt.getTime()).isEqualTo(8L * 60L * 60L * 1000L);
    }

    @SuppressWarnings("unchecked")
    private long configuredExpirationMs() {
        InputStream input = JwtServiceTest.class.getResourceAsStream("/application.yml");
        if (input == null) {
            throw new IllegalStateException("application.yml not found");
        }
        try {
            for (Object document : new Yaml().loadAll(input)) {
                if (!(document instanceof Map)) continue;
                Object daisy = ((Map<String, Object>) document).get("daisy");
                if (!(daisy instanceof Map)) continue;
                Object security = ((Map<String, Object>) daisy).get("security");
                if (!(security instanceof Map)) continue;
                String raw = String.valueOf(((Map<String, Object>) security).get("jwt-expiration-ms"));
                int separator = raw.lastIndexOf(':');
                if (separator >= 0 && raw.endsWith("}")) {
                    return Long.parseLong(raw.substring(separator + 1, raw.length() - 1));
                }
                return Long.parseLong(raw);
            }
            throw new IllegalStateException("jwt-expiration-ms not found");
        } finally {
            try {
                input.close();
            } catch (Exception ignored) {
            }
        }
    }
}
