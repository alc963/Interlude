package com.naugroup3.interlude.models.itunes;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ITunesKind {
    @JsonProperty("book") BOOK,
    @JsonProperty("album") ALBUM,
    @JsonProperty("coached-audio") COACHED_AUDIO,
    @JsonProperty("feature-movie") FEATURE_MOVIE,
    @JsonProperty("interactive-booklet") INTERACTIVE_BOOKLET,
    @JsonProperty("music-video") MUSIC_VIDEO,
    @JsonProperty("pdf") PDF,
    @JsonProperty("podcast") PODCAST,
    @JsonProperty("podcast-episode") PODCAST_EPISODE,
    @JsonProperty("software-package") SOFTWARE_PACKAGE,
    @JsonProperty("song") SONG,
    @JsonProperty("tv-episode") TV_EPISODE,
    @JsonProperty("artist") ARTIST,
    @JsonProperty("short-film") SHORT_FILM,
}
