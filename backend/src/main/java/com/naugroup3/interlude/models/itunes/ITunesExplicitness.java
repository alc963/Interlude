package com.naugroup3.interlude.models.itunes;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ITunesExplicitness {
    @JsonProperty("explicit") EXPLICIT,
    @JsonProperty("cleaned") CLEANED,
    @JsonProperty("notExplicit") NOT_EXPLICIT,
}
