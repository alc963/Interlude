package com.naugroup3.interlude.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.naugroup3.interlude.models.RefreshToken;
import com.naugroup3.interlude.models.User;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> { 
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    int deleteByUser(User user);
}
