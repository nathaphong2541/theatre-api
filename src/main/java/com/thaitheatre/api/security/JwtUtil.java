package com.thaitheatre.api.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.crypto.SecretKey;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;

public class JwtUtil {

    private final SecretKey key;
    private final long expMillis;

    public JwtUtil(String secret, long expMinutes) {
        // HS256 ต้องการ secret >= 32 bytes
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 32) {
            throw new IllegalArgumentException("JWT secret must be at least 32 bytes");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expMillis = expMinutes * 60_000;
    }

    /**
     * สร้าง token โดย subject = อะไรก็ได้ (แนะนำ: userId เป็น String)
     */
    public String generate(String subject) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(new Date(now.toEpochMilli() + expMillis))
                .signWith(key)
                .compact();
    }

    /**
     * ชัดเจน: ใช้ userId เป็น subject
     */
    public String generateForUserId(long userId) {
        return generate(String.valueOf(userId));
    }

    /**
     * สร้าง token พร้อม claims เพิ่มเติม (เช่น email, role) โดยยังใช้ userId
     * เป็น subject
     */
    public String generateWithClaims(long userId, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claims(claims)
                .issuedAt(Date.from(now))
                .expiration(new Date(now.toEpochMilli() + expMillis))
                .signWith(key)
                .compact();
    }

    /**
     * คืน subject (เช่น userId หรือ email ขึ้นกับตอน generate)
     */
    public String validateAndGetSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * ดึง userId จาก subject และแปลงเป็น Long
     */
    public Long extractUserId(String token) {
        String sub = validateAndGetSubject(token);
        try {
            return Long.valueOf(sub);
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Token subject is not a numeric user id: " + sub, e);
        }
    }

    /**
     * ใช้ภายใน: ตรวจ/ถอด token -> Claims
     */
    public Claims parseClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getExpMillis() {
        return expMillis;
    }

    public String resolveFromCookie(HttpServletRequest req, String name) {
        if (req.getCookies() == null) {
            return null;
        }
        for (var c : req.getCookies()) {
            if (name.equals(c.getName())) {
                return c.getValue();
            }
        }
        return null;
    }

}
