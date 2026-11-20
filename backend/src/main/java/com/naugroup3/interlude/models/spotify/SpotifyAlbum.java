package com.naugroup3.interlude.models.spotify;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyAlbum(
    @JsonProperty("album_type") String albumType,
    @JsonProperty("total_tracks") Integer totalTracks,
    @JsonProperty("external_urls") SpotifyExternalUrls externalUrls,
    String href,
    String id,
    List<SpotifyImage> images,
    String name,
    @JsonProperty("release_date") String releaseDate,
    @JsonProperty("release_date_precision") String releaseDatePrecision,
    SpotifyRestrictions restrictions,
    String type,
    String uri,
    List<SpotifyArtist> artists
) {}
