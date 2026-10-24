package com.naugroup3.interlude.services.Spotify;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

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
}
