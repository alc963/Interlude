package com.naugroup3.interlude.services.Spotify;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.view.RedirectView;

import com.naugroup3.interlude.models.spotify.SpotifyTokenResponse;

import java.security.SecureRandom;
import java.util.Base64;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping( "/api/auth/spotify" ) 
public class SpotifyOAuthController {

    private final SpotifyOAuthService spotifyOAuthService;

    public SpotifyOAuthController( SpotifyOAuthService spotifyOAuthService ) {
        this.spotifyOAuthService = spotifyOAuthService;
    }

    @GetMapping( "/login" )
    public RedirectView startSpotifyLogin( HttpSession session ) {
        
        byte[] stateBytes = new byte[ 32 ];
        new SecureRandom().nextBytes( stateBytes );
        
        String state = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString( stateBytes );

        session.setAttribute( "spotify_oauth_state", state );

        String authorizationUrl = spotifyOAuthService.buildAuthorizationUrl( state );
        
        return new RedirectView( authorizationUrl );
    }

    @GetMapping( "/callback" )
    public String handleSpotifyCallback(
        @RequestParam( required = false ) String code,
        @RequestParam( required = false ) String state,
        @RequestParam( required = false ) String error,
        HttpSession session 
    ) {
        Object savedState = session.getAttribute( "spotify_oauth_state" );
        session.removeAttribute( "spotify_oauth_state" );

        if( !( savedState instanceof String expectedState ) || !expectedState.equals( state ) ) {
            throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Invalid OAuth state" );
        }

        if( error != null ) {
            return "Spotify authorization was denied.";
        }

        if( code == null || code.isBlank() ) {
            throw new ResponseStatusException( HttpStatus.BAD_REQUEST, "Missing authorization code" );
        }

        // save tokens for use by api
        SpotifyTokenResponse tokens = spotifyOAuthService.exchangeCodeForTokens( code );
        session.setAttribute( "spotify_tokens", tokens );

        return "Spotify authorization succeeded.";
    }
}
