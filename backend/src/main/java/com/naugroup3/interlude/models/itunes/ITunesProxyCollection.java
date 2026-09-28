package com.naugroup3.interlude.models.itunes;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public final class ITunesProxyCollection extends ITunesResultBase {
    private Long collectionId;
    private String collectionName;
    private String collectionCensoredName;
    private String collectionViewUrl;
    private Double collectionPrice;
    private ITunesExplicitness collectionExplicitness;
    private Integer discCount;
    private Integer discNumber;
    private Integer trackCount;
    private String copyright;
    private String contentAdvisoryRating;
}
