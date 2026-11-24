package com.naugroup3.interlude.models.top;

import java.util.List;

public record TopTrack(
    String id, 
    String name, List<TopArtistRef> artists, String album,
    Integer duration, 
    long playCount, int rank, 
    String artworkUrl
) {}
