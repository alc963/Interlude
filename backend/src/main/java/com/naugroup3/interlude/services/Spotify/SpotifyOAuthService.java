package com.naugroup3.interlude.services.Spotify;

import org.springframework.web.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import com.naugroup3.interlude.models.spotify.SpotifyTokenResponse;

@Service
public class SpotifyOAuthService {

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestClient restClient;

    public SpotifyOAuthService(
        @Value( "${spring.spotify.client-id}" ) String clientId, 
        @Value( "${spring.spotify.client-secret}" ) String clientSecret,
        @Value( "${spring.spotify.redirect-uri}" ) String redirectUri ) {
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.redirectUri = redirectUri;

            this.restClient = RestClient.builder().build();
    }

    public String buildAuthorizationUrl( String state ) {
        String scopes = "user-read-currently-playing user-read-recently-played";
        // potentially later include: 
        // user-top-read, user-read-playback-state, user-read-playback-position

        return UriComponentsBuilder
            .fromUriString( "https://accounts.spotify.com/authorize?" )
            .queryParam( "response_type", "code" )
            .queryParam( "client_id", clientId )
            .queryParam( "scope", scopes )
            .queryParam( "redirect_uri", redirectUri )
            .queryParam( "state", state )
            .encode()
            .build()
            .toUriString();
    }

    public SpotifyTokenResponse exchangeCodeForTokens( String code ) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add( "grant_type", "authorization_code" );
        form.add( "code", code );
        form.add( "redirect_uri", redirectUri );
        
        return restClient.post()
            .uri( "https://accounts.spotify.com/api/token" )
            .contentType( MediaType.APPLICATION_FORM_URLENCODED )
            .headers( headers -> headers.setBasicAuth( clientId, clientSecret ) )
            .body( form )
            .retrieve()
            .body( SpotifyTokenResponse.class );
    }
}
