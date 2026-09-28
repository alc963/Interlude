package com.naugroup3.interlude.models.itunes;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ITunesProxyArtist extends ITunesResultBase {
    private Long artistId;
    private String artistName;
    private String artistType;
    private String artistLinkUrl;
    private Long amgArtistId;
}
