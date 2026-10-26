package com.naugroup3.interlude.controllers.spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;
import com.naugroup3.interlude.models.spotify.SpotifyTokenResponse;
import com.naugroup3.interlude.services.Spotify.SpotifyApiClient;

import jakarta.servlet.http.HttpSession;

@ExtendWith(MockitoExtension.class)
class SpotifyApiControllerTest {
    @Mock private SpotifyApiClient apiClient;
    @Mock private HttpSession session;
    private SpotifyApiController controller;

    @BeforeEach
    void setUp( ) {
        controller = new SpotifyApiController( apiClient );
    }

    @Test
    void currently_playing_uses_session_access_token_and_returns_playback( ) {
        SpotifyTokenResponse tokens = new SpotifyTokenResponse(
            "access-token", "Bearer", "scope", 3600, "refresh-token" );
        CurrentlyPlayingResponse playback = new CurrentlyPlayingResponse(
            true, 5000, new CurrentlyPlayingResponse.Track(
                "Track", List.of( new CurrentlyPlayingResponse.Artist( "Artist" ) ),
                new CurrentlyPlayingResponse.Album( List.of( ) ) ) );
        when( session.getAttribute( "spotify_tokens" ) ).thenReturn( tokens );
        when( apiClient.getCurrentlyPlaying( "access-token" ) ).thenReturn( playback );

        var response = controller.getCurrentlyPlaying( session );

        assertThat( response.getStatusCode( ) ).isEqualTo( HttpStatus.OK );
        assertThat( response.getBody( ) ).isSameAs( playback );
        verify( apiClient ).getCurrentlyPlaying( "access-token" );
    }

    @Test
    void currently_playing_returns_no_content_when_spotify_has_no_active_playback( ) {
        when( session.getAttribute( "spotify_tokens" ) ).thenReturn(
            new SpotifyTokenResponse( "access-token", "Bearer", "scope", 3600, "refresh-token" ) );
        when( apiClient.getCurrentlyPlaying( "access-token" ) ).thenReturn( null );

        var response = controller.getCurrentlyPlaying( session );

        assertThat( response.getStatusCode( ) ).isEqualTo( HttpStatus.NO_CONTENT );
        assertThat( response.getBody( ) ).isNull( );
    }

    @Test
    void currently_playing_rejects_session_without_spotify_tokens( ) {
        when( session.getAttribute( "spotify_tokens" ) ).thenReturn( null );

        assertThatThrownBy( ( ) -> controller.getCurrentlyPlaying( session ) )
            .isInstanceOfSatisfying( ResponseStatusException.class,
                exception -> assertThat( exception.getStatusCode( ) ).isEqualTo( HttpStatus.UNAUTHORIZED ) );

        verify( apiClient, never( ) ).getCurrentlyPlaying( anyString( ) );
    }
}
