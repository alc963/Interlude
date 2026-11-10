package com.naugroup3.interlude.controllers.spotify;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.models.spotify.CurrentlyPlayingResponse;
import com.naugroup3.interlude.models.spotify.RecentlyPlayedResponse;
import com.naugroup3.interlude.services.CurrentUserService;
import com.naugroup3.interlude.services.Spotify.SpotifyApiClient;
import com.naugroup3.interlude.services.Spotify.SpotifySessionService;

@RestController 
@RequestMapping("/spotify")
public class SpotifyApiController {

    private final SpotifyApiClient spotifyApiClient;
    private final SpotifySessionService spotifySessionService;
    private final CurrentUserService currentUserService;

    public SpotifyApiController(
        SpotifyApiClient spotifyApiClient,
        SpotifySessionService spotifySessionService,
        CurrentUserService currentUserService
    ) {
        this.spotifyApiClient = spotifyApiClient;
        this.spotifySessionService = spotifySessionService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/currently-playing")
    public ResponseEntity<CurrentlyPlayingResponse> getCurrentlyPlaying(@AuthenticationPrincipal UserDetails principal) {
        CurrentlyPlayingResponse playback = spotifyApiClient.getCurrentlyPlaying(
            requireAccessToken(principal));

        if(playback == null) return ResponseEntity.noContent().build();

        return ResponseEntity.ok(playback);
    }

    @GetMapping("/recently-played")
    public RecentlyPlayedResponse getRecentlyPlayed(
        @RequestParam(required = false) Integer limit,
        @RequestParam(required = false) Long after,
        @RequestParam(required = false) Long before,
        @AuthenticationPrincipal UserDetails principal
    ) {
        if(limit != null && (limit < 1 || limit > 50))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 50");
        if(after != null && before != null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use either after or before, not both");

        return spotifyApiClient.getRecentlyPlayed(requireAccessToken(principal), limit, after, before);
    }

    private String requireAccessToken(UserDetails principal) {
        Long userId = currentUserService.require_user_id(principal);
        return spotifySessionService.getUsersValidAccessToken(userId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Connect Spotify first"));
    }
}
