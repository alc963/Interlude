package com.naugroup3.interlude.models.spotify;

import java.util.List;

public record SpotifyTracksFetchedEvent(List<SpotifyTrack> tracks) {}
