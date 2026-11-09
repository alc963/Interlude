package com.naugroup3.interlude.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyContext(
    String type,
    String href,
    @JsonProperty("external_urls") SpotifyExternalUrls externalUrls,
    String uri
) {}
