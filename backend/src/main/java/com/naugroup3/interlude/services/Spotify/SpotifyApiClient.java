package com.naugroup3.interlude.services.Spotify;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;
import com.naugroup3.interlude.models.spotify.RecentlyPlayedResponse;
import com.naugroup3.interlude.models.spotify.SpotifyTrack;
import com.naugroup3.interlude.models.spotify.SpotifyTracksFetchedEvent;

@Service 
public class SpotifyApiClient {
    private static final String API_URL = "https://api.spotify.com/v1";
    
    private final RestClient restClient;
    private final ApplicationEventPublisher events;

    @Autowired
    public SpotifyApiClient(ApplicationEventPublisher events) {
        this(RestClient.builder().build(), events);
    }

    public SpotifyApiClient(RestClient restClient) {
        this(restClient, event -> {});
    }

    public SpotifyApiClient(RestClient restClient, ApplicationEventPublisher events) {
        this.restClient = restClient;
        this.events = events;
    }

    public CurrentlyPlayingResponse getCurrentlyPlaying(String accessToken) {
        final String uri = UriComponentsBuilder
            .fromUriString(API_URL + "/me/player/currently-playing")
            .encode()
            .toUriString();

        final CurrentlyPlayingResponse response = restClient.get()
            .uri(URI.create(uri))
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(CurrentlyPlayingResponse.class);

        if(response != null && response.item() instanceof SpotifyTrack track) {
            publish_tracks(List.of(track));
        }
        return response;
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

        final RecentlyPlayedResponse response = restClient.get()
            .uri(URI.create(uri))
            .headers(headers -> headers.setBearerAuth(accessToken))
            .retrieve()
            .body(RecentlyPlayedResponse.class);

        if(response != null && response.items() != null) {
            publish_tracks(response.items().stream()
                .map(RecentlyPlayedResponse.PlayHistory::track)
                .filter(track -> track != null)
                .toList());
        }
        return response;
    }

    private void publish_tracks(List<SpotifyTrack> tracks) {
        if(tracks.isEmpty()) return;
        events.publishEvent(new SpotifyTracksFetchedEvent(tracks));
    }
}
