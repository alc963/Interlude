package com.naugroup3.interlude.models.spotify;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RecentlyPlayedResponse(
    String href,
    Integer limit,
    String next,
    Cursors cursors,
    Integer total,
    List<PlayHistory> items
) {
    public record Cursors(String after, String before) {}
    public record PlayHistory(
        SpotifyTrack track,
        @JsonProperty("played_at") Instant playedAt,
        SpotifyContext context
    ) {}
}
