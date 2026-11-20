package com.naugroup3.interlude.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public record CurrentlyPlayingResponse(
    SpotifyDevice device,
    @JsonProperty("repeat_state") String repeatState,
    @JsonProperty("shuffle_state") Boolean shuffleState,
    SpotifyContext context,
    Long timestamp,
    @JsonProperty("progress_ms") Integer progressMs,
    @JsonProperty("is_playing") boolean isPlaying,
    SpotifyPlayable item,
    @JsonProperty("currently_playing_type") String currentlyPlayingType
) {}
