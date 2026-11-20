package com.naugroup3.interlude.services.Spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import com.naugroup3.interlude.models.spotify.SpotifySession;
import com.naugroup3.interlude.models.spotify.SpotifyTokenResponse;
import com.naugroup3.interlude.repositories.spotify.SpotifySessionRepository;

@ExtendWith(MockitoExtension.class)
class SpotifySessionServiceTest {
    @Mock private SpotifySessionRepository repository;
    @Mock private SpotifyOAuthService oauth_service;
    @InjectMocks private SpotifySessionService service;

    private SpotifySession storedSessionExpiringIn(int seconds) {
        var session = new SpotifySession(7L, new SpotifyTokenResponse("old", "Bearer", "scope", seconds, "refresh"));
        when(repository.findById(7L)).thenReturn(Optional.of(session));
        return session;
    }

    private void refreshFails(HttpStatus status) {
        when(oauth_service.refreshAccessToken("refresh")).thenThrow(HttpClientErrorException.create(status, "", null, null, null));
    }

    @Test
    void returns_empty_when_not_connected() {
        assertThat(service.getUsersValidAccessToken(7L)).isEmpty();
    }

    @Test
    void returns_stored_token_when_fresh() {
        storedSessionExpiringIn(600);
        assertThat(service.getUsersValidAccessToken(7L)).contains("old");
    }

    @Test
    void refreshes_expiring_token_and_keeps_refresh_token() {
        var session = storedSessionExpiringIn(30);
        when(oauth_service.refreshAccessToken("refresh")).thenReturn(new SpotifyTokenResponse("new", "Bearer", null, 3600, null));
        when(repository.save(session)).thenReturn(session);
        assertThat(service.getUsersValidAccessToken(7L)).contains("new");
        assertThat(session.getRefresh_token()).isEqualTo("refresh");
    }

    @Test
    void deletes_session_when_access_was_revoked() {
        var session = storedSessionExpiringIn(0);
        refreshFails(HttpStatus.BAD_REQUEST);
        assertThat(service.getUsersValidAccessToken(7L)).isEmpty();
        verify(repository).delete(session);
    }

    @Test
    void keeps_session_when_rate_limited() {
        storedSessionExpiringIn(0);
        refreshFails(HttpStatus.TOO_MANY_REQUESTS);
        assertThatThrownBy(() -> service.getUsersValidAccessToken(7L)).isInstanceOf(HttpClientErrorException.class);
        verify(repository, never()).delete(any());
    }
}
