package com.pos.security;

import com.pos.model.constants.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static volatile JwtUtil INSTANCE;

    private final SecretKey key;
    private final long ttlMillis;

    public JwtUtil(
            @Value("${auth.jwt.secret}") String secret,
            @Value("${auth.jwt.ttlSeconds}") long ttlSeconds
    ) {
        if (secret == null || secret.trim().length() < 32) {
            throw new IllegalArgumentException("auth.jwt.secret must be at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlMillis = ttlSeconds * 1000L;

        INSTANCE = this;
    }

    public static String createToken(Integer userId, UserRole role) {
        return instance().createTokenInternal(userId, role);
    }

    public static Claims parse(String token) throws JwtException {
        return instance().parseInternal(token);
    }

    private String createTokenInternal(Integer userId, UserRole role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("role", role.name())
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis)) // ✅ expiry
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseInternal(String token) throws JwtException {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private static JwtUtil instance() {
        JwtUtil ref = INSTANCE;
        if (ref == null) {
            throw new IllegalStateException("JwtUtil not initialized yet (Spring context not started).");
        }
        return ref;
    }
}
