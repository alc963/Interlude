package com.naugroup3.interlude.repositories.spotify;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.naugroup3.interlude.models.spotify.SpotifySession;

public interface SpotifySessionRepository extends JpaRepository<SpotifySession, Long> {
    @Query("SELECT s.user_id FROM SpotifySession s")
    List<Long> findAllUserIds();
}
