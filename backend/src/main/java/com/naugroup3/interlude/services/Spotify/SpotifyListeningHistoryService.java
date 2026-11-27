package com.naugroup3.interlude.services.Spotify;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;
import com.naugroup3.interlude.models.spotify.RecentlyPlayedResponse;
import com.naugroup3.interlude.models.spotify.SpotifyCurrentlyPlaying;
import com.naugroup3.interlude.models.spotify.SpotifyRecentlyPlayed;
import com.naugroup3.interlude.models.spotify.SpotifyTrack;
import com.naugroup3.interlude.repositories.spotify.SpotifyCurrentlyPlayingRepository;
import com.naugroup3.interlude.repositories.spotify.SpotifyRecentlyPlayedRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class SpotifyListeningHistoryService {
    // can't go higher
    private static final int RECENTLY_PLAYED_LIMIT = 50;

    private final SpotifyApiClient spotify_api_client;
    private final SpotifySessionService spotify_session_service;
    private final SpotifyRecentlyPlayedRepository recently_played_repo;
    private final SpotifyCurrentlyPlayingRepository currently_playing_repo;

    public SpotifyListeningHistoryService(SpotifyApiClient spotify_api_client, SpotifySessionService spotify_session_service,
            SpotifyRecentlyPlayedRepository recently_played_repo, SpotifyCurrentlyPlayingRepository currently_playing_repo) {
        this.spotify_api_client = spotify_api_client;
        this.spotify_session_service = spotify_session_service;
        this.recently_played_repo = recently_played_repo;
        this.currently_playing_repo = currently_playing_repo;
    }

    @Scheduled(initialDelayString = "${spotify.history.initial-delay-ms:30000}", fixedDelayString = "${spotify.history.poll-interval-ms:120000}")
    public void poll_users() {
        for(Long user_id : spotify_session_service.connectedUserIds()) {
            try {
                poll_user(user_id);
            } catch(RestClientException e) {
                log.warn("Failed to poll playback for user {}: {}", user_id, e.getMessage());
            }
        }
    }

    public void poll_user(Long user_id) {
        final Optional<String> access_token = spotify_session_service.getUsersValidAccessToken(user_id);
        if(access_token.isEmpty()) return;
        sync_currently_playing(user_id, access_token.get());
        sync_recently_played(user_id, access_token.get());
    }

    private void sync_recently_played(Long user_id, String access_token) {
        final Instant latest_played_time = recently_played_repo.find_latest_played_at(user_id).orElse(null);
        final Long after_time_ms = latest_played_time == null ? null : latest_played_time.toEpochMilli();
        final RecentlyPlayedResponse response = spotify_api_client.getRecentlyPlayed(access_token, RECENTLY_PLAYED_LIMIT, after_time_ms, null);
        if(response == null || response.items() == null) return;

        final List<SpotifyRecentlyPlayed> plays = response.items().stream()
            .filter(play -> play.track() != null && play.track().uri() != null && play.playedAt() != null)
            .filter(play -> latest_played_time == null || play.playedAt().isAfter(latest_played_time))
            .map(play -> new SpotifyRecentlyPlayed(user_id, play.track(), play.playedAt()))
            .toList();
        recently_played_repo.saveAll(plays);
    }

    private void sync_currently_playing(Long user_id, String access_token) {
        final CurrentlyPlayingResponse response = spotify_api_client.getCurrentlyPlaying(access_token);
        if(response == null || !(response.item() instanceof SpotifyTrack track) || track.uri() == null) {
            if(currently_playing_repo.existsById(user_id)) currently_playing_repo.deleteById(user_id);
            return;
        }
        currently_playing_repo.save(new SpotifyCurrentlyPlaying(user_id, track, response.progressMs(), response.isPlaying()));
    }
}
