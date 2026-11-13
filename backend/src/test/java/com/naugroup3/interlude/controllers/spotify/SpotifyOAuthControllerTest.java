package com.naugroup3.interlude.controllers.spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import com.naugroup3.interlude.models.spotify.SpotifyTokenResponse;
import com.naugroup3.interlude.services.CurrentUserService;
import com.naugroup3.interlude.services.Spotify.SpotifyOAuthService;
import com.naugroup3.interlude.services.Spotify.SpotifySessionService;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class SpotifyOAuthControllerTest {
    private static final String FRONTEND_URL = "http://127.0.0.1:8081";

    @Mock private SpotifyOAuthService oauth_service;
    @Mock private SpotifySessionService session_service;
    @Mock private CurrentUserService current_user_service;
    @Mock private HttpSession session;
    private SpotifyOAuthController controller;

    @BeforeEach
    void setUp() {
        controller = new SpotifyOAuthController(oauth_service, session_service, current_user_service, FRONTEND_URL);
    }

    @Test
    void login_stores_random_state_and_redirects_to_spotify() {
        when(current_user_service.require_user_id("jwt")).thenReturn(7L);
        when(oauth_service.buildAuthorizationUrl(anyString()))
            .thenReturn("https://accounts.spotify.com/authorize");

        RedirectView redirect = controller.startSpotifyLogin("jwt", session);

        ArgumentCaptor<String> state_captor = ArgumentCaptor.forClass(String.class);
        verify(session).setAttribute(eq("spotify_oauth_state"), state_captor.capture());
        verify(session).setAttribute("spotify_oauth_user_id", 7L);
        verify(oauth_service).buildAuthorizationUrl(state_captor.getValue());
        assertThat(redirect.getUrl()).isEqualTo("https://accounts.spotify.com/authorize");
    }

    @Test
    void callback_rejects_missing_or_mismatched_state() {
        when(session.getAttribute("spotify_oauth_state")).thenReturn("expected-state");

        assertThatThrownBy(() -> controller.handleSpotifyCallback("code", "wrong-state", null, session))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
        verify(session).removeAttribute("spotify_oauth_state");
        verify(oauth_service, never()).exchangeCodeForTokens(anyString());
    }

    @Test
    void callback_rejects_missing_user() {
        when(session.getAttribute("spotify_oauth_state")).thenReturn("state");
        assertThatThrownBy(() -> controller.handleSpotifyCallback("code", "state", null, session))
            .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
        verifyNoInteractions(oauth_service);
    }

    @Test
    void callback_redirects_when_user_denies_authorization() {
        when(session.getAttribute("spotify_oauth_state")).thenReturn("expected-state");
        when(session.getAttribute("spotify_oauth_user_id")).thenReturn(7L);

        RedirectView redirect = controller.handleSpotifyCallback(null, "expected-state", "access_denied", session);

        assertThat(redirect.getUrl()).isEqualTo(FRONTEND_URL + "?spotify=denied");
        verify(oauth_service, never()).exchangeCodeForTokens(anyString());
    }

    @Test
    void callback_rejects_missing_code() {
        when(session.getAttribute("spotify_oauth_state")).thenReturn("expected-state");
        when(session.getAttribute("spotify_oauth_user_id")).thenReturn(7L);

        assertThatThrownBy(() -> controller.handleSpotifyCallback(null, "expected-state", null, session))
            .isInstanceOfSatisfying(ResponseStatusException.class,
                exception -> assertThat(exception.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST));
    }

    @Test
    void callback_exchanges_code_saves_tokens_and_redirects_to_frontend() {
        SpotifyTokenResponse tokens = new SpotifyTokenResponse(
            "access-token", "Bearer", "scope", 3600, "refresh-token");

        when(session.getAttribute("spotify_oauth_state")).thenReturn("expected-state");
        when(session.getAttribute("spotify_oauth_user_id")).thenReturn(7L);
        when(oauth_service.exchangeCodeForTokens("valid-code")).thenReturn(tokens);

        RedirectView redirect = controller.handleSpotifyCallback("valid-code", "expected-state", null, session);

        assertThat(redirect.getUrl()).isEqualTo(FRONTEND_URL + "?spotify=connected");
        verify(session).removeAttribute("spotify_oauth_state");
        verify(session_service).saveTokens(7L, tokens);
        verify(oauth_service).exchangeCodeForTokens("valid-code");
    }
}
