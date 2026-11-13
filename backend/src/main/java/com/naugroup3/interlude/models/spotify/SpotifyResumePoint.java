package com.naugroup3.interlude.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyResumePoint(
    @JsonProperty("fully_played") boolean fullyPlayed,
    @JsonProperty("resume_position_ms") Integer resumePositionMs
) {}
