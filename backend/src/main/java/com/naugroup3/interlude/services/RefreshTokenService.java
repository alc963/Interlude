package com.naugroup3.interlude.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.naugroup3.interlude.exceptions.TokenRefreshException;
import com.naugroup3.interlude.models.RefreshToken;
import com.naugroup3.interlude.models.User;
import com.naugroup3.interlude.repositories.RefreshTokenRepository;
import com.naugroup3.interlude.repositories.UserRepository;

@Service
public class RefreshTokenService {
    private final long duration_ms;
    private final RefreshTokenRepository refresh_token_repository;
    private final UserRepository user_repository;

    public RefreshTokenService(
            RefreshTokenRepository refresh_token_repository,
            UserRepository user_repository,
            @Value("${jwt.refresh-expiration-ms:86400000}") long duration_ms) {
        this.refresh_token_repository = refresh_token_repository;
        this.user_repository = user_repository;
        this.duration_ms = duration_ms;
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refresh_token_repository.findByToken(token);
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId) {
        User user = user_repository.findById(userId)
                .orElseThrow(() -> new TokenRefreshException("User for refresh token was not found"));
        refresh_token_repository.deleteByUser(user);

        RefreshToken refresh_token = new RefreshToken();
        refresh_token.setUser(user);
        refresh_token.setExpiryDate(Instant.now().plusMillis(duration_ms));
        refresh_token.setToken(UUID.randomUUID().toString());

        return refresh_token_repository.save(refresh_token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (!token.getExpiryDate().isAfter(Instant.now())) {
            refresh_token_repository.delete(token);
            throw new TokenRefreshException("Refresh token expired; please sign in again");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return user_repository.findById(userId)
                .map(refresh_token_repository::deleteByUser)
                .orElse(0);
    }
}
