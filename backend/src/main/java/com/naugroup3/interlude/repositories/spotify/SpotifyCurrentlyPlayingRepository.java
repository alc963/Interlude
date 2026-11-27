package com.naugroup3.interlude.repositories.spotify;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naugroup3.interlude.models.spotify.SpotifyCurrentlyPlaying;

public interface SpotifyCurrentlyPlayingRepository extends JpaRepository<SpotifyCurrentlyPlaying, Long> {
}
