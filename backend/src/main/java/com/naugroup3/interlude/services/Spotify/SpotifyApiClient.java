package com.naugroup3.interlude.services.Spotify;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClient;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;

@Service 
public class SpotifyApiClient {
    
    private final RestClient restClient;

    @Autowired
    public SpotifyApiClient() {
        this(RestClient.builder().build());
    }

    public SpotifyApiClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public CurrentlyPlayingResponse getCurrentlyPlaying( String accessToken ) {
        return restClient.get()
            .uri( "https://api.spotify.com/v1/me/player/currently-playing")
            .headers( headers -> headers.setBearerAuth( accessToken ) )
            .retrieve()
            .body( CurrentlyPlayingResponse.class );
    }
}
