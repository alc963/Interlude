package com.naugroup3.interlude.services;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;

import com.naugroup3.interlude.models.RefreshToken;
import com.naugroup3.interlude.repositories.RefreshTokenRepository;
import com.naugroup3.interlude.repositories.UserRepository;

@Service
public class RefreshTokenService {
    final private Long DURATION_MS = 1000L * 60L * 60L * 24L; // 1 day in milliseconds

    private RefreshTokenRepository refreshTokenRepository;

    private UserRepository userRepository;

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(DURATION_MS));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(),
                    "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        return refreshTokenRepository.deleteByUser(userRepository.findById(userId).get());
    }

}
