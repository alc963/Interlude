package com.naugroup3.interlude.repositories.spotify;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.naugroup3.interlude.models.spotify.SpotifyRecentlyPlayed;

public interface SpotifyRecentlyPlayedRepository extends JpaRepository<SpotifyRecentlyPlayed, Long> {
    @Query("SELECT MAX(p.played_at) FROM SpotifyRecentlyPlayed p WHERE p.user_id = ?1")
    Optional<Instant> find_latest_played_at(Long user_id);

    @Query("SELECT p.track_uri FROM SpotifyRecentlyPlayed p WHERE p.user_id = ?1 ORDER BY p.played_at DESC")
    List<String> find_recent_track_uris(Long user_id, Pageable page);
}
