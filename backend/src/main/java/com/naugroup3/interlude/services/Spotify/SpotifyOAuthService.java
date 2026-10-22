package com.naugroup3.interlude.services.Spotify;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SpotifyOAuthService {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public SpotifyOAuthService(
        @Value( "${spring.spotify.client-id}" ) String clientId, 
        @Value( "${spring.spotify.client-secret}" ) String clientSecret,
        @Value( "${spring.spotify.redirect-uri}" ) String redirectUri ) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.redirectUri = redirectUri;
    }

    public String buildAuthorizationUrl( String state ) {
        String scopes = "user-read-currently-playing user-read-recently-played";
        // potentially later include: 
        // user-top-read, user-read-playback-state, user-read-playback-position

        return UriComponentsBuilder
            .fromUriString("https://accounts.spotify.com/authorize")
            .queryParam("client_id", clientId)
            .queryParam("response_type", "code")
            .queryParam("redirect_uri", redirectUri)
            .queryParam("state", state)
            .queryParam("scope", scopes)
            .encode()
            .build()
            .toUriString();
    }
}
