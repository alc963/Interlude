package com.naugroup3.interlude.models.spotify;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo( use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type", visible = true, defaultImpl = Void.class )
@JsonSubTypes( {@JsonSubTypes.Type( value = SpotifyTrack.class, name = "track" ), } )
public sealed interface SpotifyPlayable permits SpotifyTrack {
    String id();
    String name();
    String type();
    String uri();
}
