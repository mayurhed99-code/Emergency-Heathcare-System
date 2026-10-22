package com.emergency.platform.security;

import com.emergency.platform.entity.AppUser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {
    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationMs;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration}") long expirationMs) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationMs = expirationMs;
    }

    public String generateToken(AppUser user) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("sub", user.getEmail());
            payload.put("role", user.getRole().name());
            payload.put("name", user.getName());
            payload.put("exp", Instant.now().plusMillis(expirationMs).getEpochSecond());
            String head = encode(objectMapper.writeValueAsBytes(header));
            String body = encode(objectMapper.writeValueAsBytes(payload));
            return head + "." + body + "." + sign(head + "." + body);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate token", exception);
        }
    }

    public String extractSubject(String token) {
        return claims(token).get("sub").toString();
    }

    public boolean isValid(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return false;
            }
            if (!sign(parts[0] + "." + parts[1]).equals(parts[2])) {
                return false;
            }
            Number exp = (Number) claims(token).get("exp");
            return Instant.now().getEpochSecond() < exp.longValue();
        } catch (Exception exception) {
            return false;
        }
    }

    private Map<String, Object> claims(String token) {
        try {
            String[] parts = token.split("\\.");
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            return objectMapper.readValue(decoded, new TypeReference<>() {
            });
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token", exception);
        }
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return encode(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }
}
