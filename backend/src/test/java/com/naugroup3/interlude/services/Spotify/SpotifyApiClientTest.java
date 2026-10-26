package com.naugroup3.interlude.services.Spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;

class SpotifyApiClientTest {
    private MockRestServiceServer mockServer;
    private SpotifyApiClient client;

    @BeforeEach
    void setUp( ) {
        RestClient.Builder builder = RestClient.builder( );
        mockServer = MockRestServiceServer.bindTo( builder ).build( );
        client = new SpotifyApiClient( builder.build( ) );
    }

    @AfterEach
    void verifyRequests( ) {
        mockServer.verify( );
    }

    @Test
    void gets_currently_playing_with_bearer_token_and_maps_response( ) {
        mockServer.expect( requestTo( "https://api.spotify.com/v1/me/player/currently-playing" ) )
            .andExpect( method( HttpMethod.GET ) )
            .andExpect( header( HttpHeaders.AUTHORIZATION, "Bearer spotify-access-token" ) )
            .andRespond( withSuccess( """
                {
                  "is_playing": true,
                  "progress_ms": 12345,
                  "item": {
                    "name": "Test Track",
                    "artists": [{"name": "Test Artist"}],
                    "album": {"images": [{"url": "https://example.com/cover.jpg"}]}
                  }
                }
                """, MediaType.APPLICATION_JSON ) );

        CurrentlyPlayingResponse response = client.getCurrentlyPlaying( "spotify-access-token" );

        assertThat( response.isPlaying( ) ).isTrue( );
        assertThat( response.progressMs( ) ).isEqualTo( 12345 );
        assertThat( response.item( ).name( ) ).isEqualTo( "Test Track" );
        assertThat( response.item( ).artists( ) ).extracting( CurrentlyPlayingResponse.Artist::name )
            .containsExactly( "Test Artist" );
        assertThat( response.item( ).album( ).images( ) ).extracting( CurrentlyPlayingResponse.Image::url )
            .containsExactly( "https://example.com/cover.jpg" );
    }

    @Test
    void returns_null_when_spotify_has_no_current_playback( ) {
        mockServer.expect( requestTo( "https://api.spotify.com/v1/me/player/currently-playing" ) )
            .andExpect( method( HttpMethod.GET ) )
            .andExpect( header( HttpHeaders.AUTHORIZATION, "Bearer spotify-access-token" ) )
            .andRespond( withNoContent( ) );

        assertThat( client.getCurrentlyPlaying( "spotify-access-token" ) ).isNull( );
    }
}
