package com.naugroup3.interlude.models.itunes;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public sealed class ITunesResultBase permits ITunesProxyArtist, ITunesProxyCollection, ITunesProxyTrack {
    private ITunesWrapperType wrapperType;
    private ITunesKind kind;
    private Long artistId;
    private String artistName;
    private String artistViewUrl;
    private String artworkUrl30;
    private String artworkUrl60;
    private String artworkUrl100;
    private String artworkUrl600;
    private String country;
    private String currency;
    private String primaryGenreName;
    private Long primaryGenreId;
    private List<String> genreIds;
    private List<String> genres;
    private String releaseDate;
}
