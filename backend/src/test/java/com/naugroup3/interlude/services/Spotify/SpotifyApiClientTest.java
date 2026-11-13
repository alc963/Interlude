package com.naugroup3.interlude.services.Spotify;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withNoContent;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Instant;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;
import com.naugroup3.interlude.models.spotify.SpotifyArtist;
import com.naugroup3.interlude.models.spotify.SpotifyImage;
import com.naugroup3.interlude.models.spotify.SpotifyTrack;

class SpotifyApiClientTest {
    private MockRestServiceServer mock_server;
    private SpotifyApiClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        mock_server = MockRestServiceServer.bindTo(builder).build();
        client = new SpotifyApiClient(builder.build());
    }

    @AfterEach
    void verifyRequests() {
        mock_server.verify();
    }

    @Test
    void gets_currently_playing_with_bearer_token_and_maps_response() {
        mock_server.expect(requestTo("https://api.spotify.com/v1/me/player/currently-playing"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer spotify-access-token"))
            .andRespond(withSuccess("""
                {
                  "is_playing": true,
                  "progress_ms": 12345,
                  "item": {
                    "type": "track",
                    "name": "Test Track",
                    "explicit": false,
                    "is_local": false,
                    "artists": [{"name": "Test Artist"}],
                    "album": {"images": [{"url": "https://example.com/cover.jpg"}]}
                  }
                }
                """, MediaType.APPLICATION_JSON));

        CurrentlyPlayingResponse response = client.getCurrentlyPlaying("spotify-access-token");

        assertThat(response.isPlaying()).isTrue();
        assertThat(response.progressMs()).isEqualTo(12345);
        SpotifyTrack track = (SpotifyTrack) response.item();
        assertThat(track.name()).isEqualTo("Test Track");
        assertThat(track.artists()).extracting(SpotifyArtist::name)
            .containsExactly("Test Artist");
        assertThat(track.album().images()).extracting(SpotifyImage::url)
            .containsExactly("https://example.com/cover.jpg");
    }

    @Test
    void returns_null_when_spotify_has_no_current_playback() {
        mock_server.expect(requestTo("https://api.spotify.com/v1/me/player/currently-playing"))
            .andExpect(method(HttpMethod.GET))
            .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer spotify-access-token"))
            .andRespond(withNoContent());

        assertThat(client.getCurrentlyPlaying("spotify-access-token")).isNull();
    }

    @Test
    void currently_playing_ignores_episodes() {
        mock_server.expect(requestTo("https://api.spotify.com/v1/me/player/currently-playing"))
            .andRespond(withSuccess("{\"is_playing\": true, \"item\": {\"type\": \"episode\"}}", MediaType.APPLICATION_JSON));
        assertThat(client.getCurrentlyPlaying("token").item()).isNull();
    }

    @Test
    void recently_played_sends_cursor_and_maps_items() {
        mock_server.expect(requestTo("https://api.spotify.com/v1/me/player/recently-played?limit=2&before=100"))
            .andRespond(withSuccess("""
                {"items": [{"played_at": "2023-11-14T22:13:20Z",
                  "track": {"type": "track", "name": "Track", "explicit": false, "is_local": false}}]}""",
                MediaType.APPLICATION_JSON));
        var item = client.getRecentlyPlayed("token", 2, null, 100L).items().get(0);
        assertThat(item.track().name()).isEqualTo("Track");
        assertThat(item.playedAt()).isEqualTo(Instant.parse("2023-11-14T22:13:20Z"));
    }
}
