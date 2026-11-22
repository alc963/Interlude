package com.naugroup3.interlude.models.spotify;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SpotifyDevice(
    String id,
    @JsonProperty("is_active") boolean isActive,
    @JsonProperty("is_private_session") boolean isPrivateSession,
    @JsonProperty("is_restricted") boolean isRestricted,
    String name,
    String type,
    @JsonProperty("volume_percent") Integer volumePercent,
    @JsonProperty("supports_volume") boolean supportsVolume
) {}
