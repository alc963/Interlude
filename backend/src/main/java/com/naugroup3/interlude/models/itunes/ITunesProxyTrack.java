package com.naugroup3.interlude.models.itunes;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ITunesProxyTrack extends ITunesResultBase {
    private Long collectionId;
    private String collectionName;
    private String collectionCensoredName;
    private String collectionViewUrl;
    private Long trackId;
    private String trackName;
    private String trackCensoredName;
    private String trackViewUrl;
    private String previewUrl;
    private Double collectionPrice;
    private Double trackPrice;
    private ITunesExplicitness trackExplicitness;
    private ITunesExplicitness collectionExplicitness;
    private Integer discCount;
    private Integer discNumber;
    private Integer trackCount;
    private Integer trackNumber;
    private Long trackTimeMillis;
    private Boolean isStreamable;
}
