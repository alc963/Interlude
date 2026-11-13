package com.naugroup3.interlude.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyArtist(
    @JsonProperty("external_urls") SpotifyExternalUrls externalUrls,
    String href,
    String id,
    String name,
    String type,
    String uri
) {}
