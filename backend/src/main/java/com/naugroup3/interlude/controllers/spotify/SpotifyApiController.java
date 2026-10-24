package com.naugroup3.interlude.controllers.spotify;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;
import com.naugroup3.interlude.models.spotify.SpotifyTokenResponse;
import com.naugroup3.interlude.services.Spotify.SpotifyApiClient;

import jakarta.servlet.http.HttpSession;

@RestController 
@RequestMapping( "/spotify" )
public class SpotifyApiController {

    private final SpotifyApiClient spotifyApiClient;

    public SpotifyApiController( SpotifyApiClient spotifyApiClient ) {
        this.spotifyApiClient = spotifyApiClient;
    }
        
    @GetMapping( "/currently-playing" )
    public ResponseEntity<CurrentlyPlayingResponse> getCurrentlyPlaying( HttpSession session ) {
        Object storedTokens = session.getAttribute( "spotify_tokens" );

        if( !( storedTokens instanceof SpotifyTokenResponse tokens ) ) {
            throw new ResponseStatusException( HttpStatus.UNAUTHORIZED, "Connect Spotify first" );
        }

        CurrentlyPlayingResponse playback = spotifyApiClient.getCurrentlyPlaying( tokens.accessToken() );

        if( playback == null ) {
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok( playback );
    }
}
