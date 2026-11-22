package com.naugroup3.interlude.services.Spotify;

import java.net.URI;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;
import com.naugroup3.interlude.models.spotify.RecentlyPlayedResponse;

@Service 
public class SpotifyApiClient {
    private static final String API_URL = "https://api.spotify.com/v1";
    
    private final RestClient restClient;

    @Autowired
    public SpotifyApiClient() {
        this(RestClient.builder().build());
    }

    public SpotifyApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public CurrentlyPlayingResponse getCurrentlyPlaying(String accessToken) {
        final String uri = UriComponentsBuilder
            .fromUriString(API_URL + "/me/player/currently-playing")
            .encode()
            .toUriString();

        return restClient.get()
            .uri(URI.create(uri))
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(CurrentlyPlayingResponse.class);
    }

    // after and before are unix ms cursors, at most one may be set
    public RecentlyPlayedResponse getRecentlyPlayed(String accessToken, Integer limit, Long after, Long before) {
        final String uri = UriComponentsBuilder
            .fromUriString(API_URL + "/me/player/recently-played")
            .queryParamIfPresent("limit", Optional.ofNullable(limit))
            .queryParamIfPresent("after", Optional.ofNullable(after))
            .queryParamIfPresent("before", Optional.ofNullable(before))
            .encode()
            .toUriString();

        return restClient.get()
            .uri(URI.create(uri))
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(RecentlyPlayedResponse.class);
    }
}
