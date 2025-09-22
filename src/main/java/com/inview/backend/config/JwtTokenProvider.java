package com.inview.backend.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Duration;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret; // 최소 256-bit

    @Value("${jwt.issuer:}")              // ← 빈 문자열이면 issuer 비강제
    private String issuer;

    @Value("${jwt.validHours:12}")
    private long validHours;

    @Value("${jwt.clockSkewSeconds:60}")
    private long clockSkewSeconds;

    private Key key;
    private JwtParser parser;
    private long validityMs;

    @PostConstruct
    public void init() {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret is missing");
        }

        String s = secret.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1);
        }

        byte[] keyBytes;
        try {
            if (s.regionMatches(true, 0, "base64:", 0, 7)) {
                keyBytes = Decoders.BASE64.decode(s.substring(7));
            } else if (s.matches("^[A-Za-z0-9+/=]+$") && s.length() >= 44) {
                keyBytes = Decoders.BASE64.decode(s);
            } else {
                keyBytes = s.getBytes(StandardCharsets.UTF_8);
            }
        } catch (IllegalArgumentException e) {
            keyBytes = s.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 256-bit (32 bytes).");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.validityMs = Duration.ofHours(validHours > 0 ? validHours : 12).toMillis();

        // ✅ issuer를 파서에서 강제하지 않음(기존 토큰 호환)
        this.parser = Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(Math.max(0, clockSkewSeconds))
                .build();
    }

    /** 액세스 토큰 생성 */
    public String createToken(String userId) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + validityMs);

        JwtBuilder b = Jwts.builder()
                .setId(UUID.randomUUID().toString())
                .setSubject(userId)
                .setIssuedAt(now)
                .setNotBefore(now)
                .setExpiration(exp);

        if (issuer != null && !issuer.isBlank()) {
            b.setIssuer(issuer);
        }

        return b.signWith(key, SignatureAlgorithm.HS256).compact();
    }

    /** 토큰에서 userId(subject) 추출 */
    public String getUserId(String token) {
        if (token == null || token.isBlank()) {
            throw new IllegalArgumentException("token is blank");
        }
        Claims claims = parser.parseClaimsJws(stripBearer(token)).getBody();
        // ✅ issuer가 설정돼 있으면 일치 여부만 체크(없으면 통과)
        if (issuer != null && !issuer.isBlank()) {
            String iss = claims.getIssuer();
            if (iss == null || !issuer.equals(iss)) {
                throw new JwtException("Invalid issuer");
            }
        }
        return claims.getSubject();
    }

    /** 형식/서명/만료 유효성 */
    public boolean validateToken(String token) {
        if (token == null || token.isBlank()) return false;
        try {
            Claims claims = parser.parseClaimsJws(stripBearer(token)).getBody();
            if (issuer != null && !issuer.isBlank()) {
                String iss = claims.getIssuer();
                if (iss == null || !issuer.equals(iss)) return false;
            }
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private static String stripBearer(String token) {
        String t = token.trim();
        return t.startsWith("Bearer ") ? t.substring(7) : t;
    }
}
