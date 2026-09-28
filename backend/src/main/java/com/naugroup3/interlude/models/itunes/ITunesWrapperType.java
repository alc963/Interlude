package com.naugroup3.interlude.models.itunes;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ITunesWrapperType {
    @JsonProperty("track") TRACK,
    @JsonProperty("collection") COLLECTION,
    @JsonProperty("artist") ARTIST,
    @JsonProperty("audiobook") AUDIOBOOK,
}
