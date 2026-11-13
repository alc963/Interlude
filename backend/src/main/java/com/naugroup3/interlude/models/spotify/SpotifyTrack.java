package com.naugroup3.interlude.models.spotify;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyTrack(
    SpotifyAlbum album,
    List<SpotifyArtist> artists,
    @JsonProperty("disc_number") Integer discNumber,
    @JsonProperty("duration_ms") Integer durationMs,
    boolean explicit,
    @JsonProperty("external_ids") SpotifyExternalIds externalIds,
    @JsonProperty("external_urls") SpotifyExternalUrls externalUrls,
    String href,
    String id,
    @JsonProperty("is_playable") Boolean isPlayable,
    SpotifyRestrictions restrictions,
    String name,
    @JsonProperty("track_number") Integer trackNumber,
    String type,
    String uri,
    @JsonProperty("is_local") boolean isLocal
) implements SpotifyPlayable {}
