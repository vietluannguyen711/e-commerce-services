package com.example.e_commerce_services.service;

import java.security.Key;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final Key key;
    private final long accessExpMinutes;
    private final long refreshExpDays;

    public JwtService(@Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.access-exp-minutes}") long accessExpMinutes,
            @Value("${app.jwt.refresh-exp-days}") long refreshExpDays) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessExpMinutes = accessExpMinutes;
        this.refreshExpDays = refreshExpDays;
    }

    public String generateAccessToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setAudience("access")
                .addClaims(Map.of("email", email))
                .setIssuedAt(new Date())
                .setExpiration(Date.from(OffsetDateTime.now().plusMinutes(accessExpMinutes).toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateRefreshToken(Long userId, String email) {
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setAudience("refresh")
                .addClaims(Map.of("email", email))
                .setIssuedAt(new Date())
                .setExpiration(Date.from(OffsetDateTime.now().plusDays(refreshExpDays).toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parse(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }
}
