package com.naugroup3.interlude.models.spotify;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

// **note - way more metadata available if we want later

public record CurrentlyPlayingResponse(
    @JsonProperty( "is_playing" ) boolean isPlaying,
    @JsonProperty( "progress_ms" ) Integer progressMs,
    Track item
) {
    public record Track(
        String name,
        List<Artist> artists,
        Album album
    ) {}

    public record Artist( String name ) {}

    public record Album( List<Image> images ) {}

    public record Image( String url ) {}
} 