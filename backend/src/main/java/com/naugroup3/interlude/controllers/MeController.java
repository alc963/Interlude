package com.naugroup3.interlude.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.naugroup3.interlude.models.User;
import com.naugroup3.interlude.models.top.TopAlbum;
import com.naugroup3.interlude.models.top.TopArtist;
import com.naugroup3.interlude.models.top.TopArtistSummary;
import com.naugroup3.interlude.models.top.TopTrack;
import com.naugroup3.interlude.repositories.UserRepository;
import com.naugroup3.interlude.services.CurrentUserService;
import com.naugroup3.interlude.services.TopItemsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MeController {
    public record MeResponse(String username, String email) {
    }

    private final UserRepository user_repository;
    private final CurrentUserService current_user_service;
    private final TopItemsService top_items_service;

    @GetMapping
    public MeResponse me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Not logged in");
        final User user = user_repository.findByEmail(principal.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        return new MeResponse(user.getUsername(), user.getEmail());
    }

    @GetMapping("/top/tracks")
    public List<TopTrack> topTracks(@RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal UserDetails principal) {
        validate_limit(limit);
        return top_items_service.top_tracks(current_user_service.require_user_id(principal), limit);
    }

    @GetMapping("/top/artists")
    public List<TopArtist> topArtists(@RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal UserDetails principal) {
        validate_limit(limit);
        return top_items_service.top_artists(current_user_service.require_user_id(principal), limit);
    }

    @GetMapping("/top/artists/summary")
    public TopArtistSummary topArtistSummary(@RequestParam String id, @RequestParam(defaultValue = "5") int limit, @AuthenticationPrincipal UserDetails principal) {
        validate_limit(limit);
        return top_items_service.top_artist_summary(current_user_service.require_user_id(principal), id, limit);
    }

    @GetMapping("/top/albums")
    public List<TopAlbum> topAlbums(@RequestParam(defaultValue = "10") int limit, @AuthenticationPrincipal UserDetails principal) {
        validate_limit(limit);
        return top_items_service.top_albums(current_user_service.require_user_id(principal), limit);
    }

    private static void validate_limit(int limit) {
        if (limit < 1 || limit > 50) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "limit must be between 1 and 50");
    }
}
