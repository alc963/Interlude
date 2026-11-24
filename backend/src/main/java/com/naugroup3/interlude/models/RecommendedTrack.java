package com.naugroup3.interlude.models;

public record RecommendedTrack(
    Long id, String name, String artist, String album,
    String genre, String mood,
    String artwork_url, String preview_url, String view_url,
    double score
) {}
