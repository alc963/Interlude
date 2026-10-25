package com.naugroup3.interlude.utils.jwt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

class JWTUtilsTest {
    private static final String SECRET = "test-secret-key-that-is-at-least-32-bytes-long";
    private static final String OTHER_SECRET = "a-different-secret-key-that-is-also-32-bytes";

    private JWTUtils jwt_utils;

    @BeforeEach
    void setUp() {
        jwt_utils = new JWTUtils();
        ReflectionTestUtils.setField(jwt_utils, "SECRET_KEY", SECRET);
    }

    private static UserDetails user(String username) {
        return User.withUsername(username).password("password").authorities("USER").build();
    }

    private static String token_with(String secret, String subject, Date expiration) {
        return Jwts.builder()
            .setSubject(subject)
            .setIssuedAt(new Date())
            .setExpiration(expiration)
            .signWith(Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
            .compact();
    }

    @Test
    void generated_token_expires_in_sixty_days() {
        final String token = jwt_utils.generate_token("user@example.com");
        final long sixty_days_ms = 1000L * 60 * 60 * 24 * 60;
        final long exp = Jwts.parserBuilder()
            .setSigningKey(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8)))
            .build().parseClaimsJws(token).getBody().getExpiration().getTime();
        assertThat(exp - System.currentTimeMillis()).isBetween(sixty_days_ms - 5_000, sixty_days_ms);
    }

    @Test
    void validates_token_for_matching_user() {
        final String token = jwt_utils.generate_token("user@example.com");
        assertThat(jwt_utils.validate_token(token, user("user@example.com"))).isTrue();
    }

    @Test
    void rejects_token_for_different_user() {
        final String token = jwt_utils.generate_token("user@example.com");
        assertThat(jwt_utils.validate_token(token, user("other@example.com"))).isFalse();
    }

    @Test
    void rejects_expired_token() {
        final String token = token_with(SECRET, "user@example.com", new Date(System.currentTimeMillis() - 60_000));
        assertThatThrownBy(() -> jwt_utils.extract_email(token)).isInstanceOf(ExpiredJwtException.class);
        assertThatThrownBy(() -> jwt_utils.validate_token(token, user("user@example.com")))
            .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void rejects_token_signed_with_another_secret() {
        final String token = token_with(OTHER_SECRET, "user@example.com", new Date(System.currentTimeMillis() + 60_000));
        assertThatThrownBy(() -> jwt_utils.extract_email(token)).isInstanceOf(JwtException.class);
    }

    @Test
    void rejects_tampered_token() {
        final String token = jwt_utils.generate_token("user@example.com");
        final String[] parts = token.split("\\.");
        final String forged_payload = java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString("{\"sub\":\"admin@example.com\",\"exp\":99999999999}".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> jwt_utils.extract_email(parts[0] + "." + forged_payload + "." + parts[2]))
            .isInstanceOf(JwtException.class);
    }

    @Test
    void rejects_malformed_token() {
        assertThatThrownBy(() -> jwt_utils.extract_email("not-a-jwt")).isInstanceOf(JwtException.class);
        assertThatThrownBy(() -> jwt_utils.extract_email("")).isInstanceOf(IllegalArgumentException.class);
    }
}
