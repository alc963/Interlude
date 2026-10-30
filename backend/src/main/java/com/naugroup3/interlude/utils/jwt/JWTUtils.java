package com.naugroup3.interlude.utils.jwt;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JWTUtils {
    @Value("${jwt.secret}")
    private String SECRET_KEY;
    // TODO add TimeUtils
    // 15 minutes in milliseconds
    private final long EXPIRATION_TIME_MS = 1000L * 60L * 15L;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    public String generate_token(String username) {
        return Jwts.builder()
            .setSubject(username)
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
            .signWith(getSigningKey(), SignatureAlgorithm.HS256)
            .compact();
    }

    private Claims get_token_body(String token) {
        return Jwts.parserBuilder()
            .setSigningKey(getSigningKey())
            .build()
            .parseClaimsJws(token)
            .getBody();
    }

    public String extract_email(String token) {
        return get_token_body(token).getSubject();
    }

    private boolean is_token_expired(String token) {
        final Date expiration = get_token_body(token).getExpiration();
        return expiration.before(new Date());
    }

    public boolean validate_token(String token, UserDetails user_details) {
        final String username = extract_email(token);
        return (username.equals(user_details.getUsername()) && !is_token_expired(token));
    }
}
