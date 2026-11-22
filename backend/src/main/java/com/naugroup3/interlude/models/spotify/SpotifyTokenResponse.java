package com.naugroup3.interlude.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("scope") String scope,
    @JsonProperty("expires_in") int expiresIn,
    @JsonProperty("refresh_token") String refreshToken
) {}
