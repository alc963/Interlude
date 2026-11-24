package com.naugroup3.interlude.models.top;

import java.util.List;

public record TopAlbum(String id, String name, List<TopArtistRef> artists, String artworkUrl, long playCount) {}
