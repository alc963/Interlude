package com.naugroup3.interlude.services.Spotify;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import com.naugroup3.interlude.models.spotify.SpotifySession;
import com.naugroup3.interlude.models.spotify.SpotifyTokenResponse;
import com.naugroup3.interlude.repositories.spotify.SpotifySessionRepository;

// to act on behalf of users
@Service
public class SpotifySessionService {
    private static final Duration REFRESH_EARLY_DURATION = Duration.ofSeconds(60);

    private final SpotifySessionRepository repository;
    private final SpotifyOAuthService spotify_oauth_service;

    public SpotifySessionService(SpotifySessionRepository repository, SpotifyOAuthService spotify_oauth_service) {
        this.repository = repository;
        this.spotify_oauth_service = spotify_oauth_service;
    }

    // connecting again replaces the user's previous tokens
    public void saveTokens(Long userId, SpotifyTokenResponse tokens) {
        repository.save(new SpotifySession(userId, tokens));
    }

    public List<Long> connectedUserIds() {
        return repository.findAllUserIds();
    }

    public Optional<String> getUsersValidAccessToken(Long user_id) {
        final SpotifySession session = repository.findById(user_id).orElse(null);
        if(session == null) return Optional.empty();
        // token is fine
        if(session.getExpires_at().isAfter(Instant.now().plus(REFRESH_EARLY_DURATION)))
            return Optional.of(session.getAccess_token());

        try {
            // new tokens
            session.apply_tokens(spotify_oauth_service.refreshAccessToken(session.getRefresh_token()));
            return Optional.of(repository.save(session).getAccess_token());
        } catch(HttpClientErrorException.BadRequest e) {
            repository.delete(session);
            return Optional.empty();
        }
    }
}
