package com.naugroup3.interlude.controllers.spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;
import com.naugroup3.interlude.models.spotify.RecentlyPlayedResponse;
import com.naugroup3.interlude.services.CurrentUserService;
import com.naugroup3.interlude.services.Spotify.SpotifyApiClient;
import com.naugroup3.interlude.services.Spotify.SpotifySessionService;

@ExtendWith(MockitoExtension.class)
class SpotifyApiControllerTest {
    @Mock private SpotifyApiClient api_client;
    @Mock private SpotifySessionService session_service;
    @Mock private CurrentUserService current_user_service;
    @Mock private UserDetails principal;
    private SpotifyApiController controller;

    @BeforeEach
    void setUp() {
        controller = new SpotifyApiController(api_client, session_service, current_user_service);
    }

    @Test
    void currently_playing_uses_session_access_token_and_returns_playback() {
        CurrentlyPlayingResponse playback = new CurrentlyPlayingResponse(
            null, null, null, null, null, 5000, true, null, "track");

        when(current_user_service.require_user_id(principal)).thenReturn(7L);
        when(session_service.getUsersValidAccessToken(7L)).thenReturn(Optional.of("access-token"));
        when(api_client.getCurrentlyPlaying("access-token")).thenReturn(playback);

        var response = controller.getCurrentlyPlaying(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isSameAs(playback);
        verify(api_client).getCurrentlyPlaying("access-token");
    }

    @Test
    void currently_playing_returns_no_content_when_spotify_has_no_active_playback() {
        when(current_user_service.require_user_id(principal)).thenReturn(7L);
        when(session_service.getUsersValidAccessToken(7L)).thenReturn(Optional.of("access-token"));
        when(api_client.getCurrentlyPlaying("access-token")).thenReturn(null);

        var response = controller.getCurrentlyPlaying(principal);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
    }

    @Test
    void currently_playing_rejects_session_without_spotify_tokens() {
        when(current_user_service.require_user_id(principal)).thenReturn(7L);
        when(session_service.getUsersValidAccessToken(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> controller.getCurrentlyPlaying(principal))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED));
        verify(api_client, never()).getCurrentlyPlaying(anyString());
    }

    @Test
    void recently_played_passes_query_through() {
        var recent = new RecentlyPlayedResponse(null, 10, null, null, null, List.of());
        when(current_user_service.require_user_id(principal)).thenReturn(7L);
        when(session_service.getUsersValidAccessToken(7L)).thenReturn(Optional.of("token"));
        when(api_client.getRecentlyPlayed("token", 10, null, 100L)).thenReturn(recent);
        assertThat(controller.getRecentlyPlayed(10, null, 100L, principal)).isSameAs(recent);
    }

    @ParameterizedTest
    @CsvSource({"0,,", "51,,", ",1,2"})
    void recently_played_rejects_bad_query(Integer limit, Long after, Long before) {
        assertThatThrownBy(() -> controller.getRecentlyPlayed(limit, after, before, principal))
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }
}
