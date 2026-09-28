package com.naugroup3.interlude.models.itunes;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.naugroup3.interlude.utils.Utils;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "itunes_track")
public final class ITunesTrack {
    @Id
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = true, length = 200)
    private String censored_name;
    @Column(unique = true, nullable = false, length = 300)
    private String view_url;
    @Column(nullable = true, length = 500)
    private String preview_url;

    @Column(nullable = true, length = 200)
    private String artwork_url_60;
    @Column(nullable = true, length = 200)
    private String artwork_url_100;

    @Column(nullable = true)
    private Double price;
    @Enumerated(EnumType.STRING)
    @Column(nullable = true, length = 20)
    private ITunesExplicitness explicitness;

    @Column(nullable = true)
    private Integer disc_count;
    @Column(nullable = true)
    private Integer disc_number;
    @Column(nullable = true)
    private Integer track_count;
    @Column(nullable = true)
    private Integer track_number;
    @Column(nullable = true)
    private Long track_time_millis;

    @Column(nullable = true)
    private Boolean is_streamable;

    @Column(nullable = true, length = 10)
    private String country;
    @Column(nullable = true, length = 10)
    private String currency;
    @Column(nullable = true)
    private LocalDateTime release_date;
    @Column(nullable = true, length = 80)
    private String primary_genre_name;

    @Column(name = "artist_id", nullable = true)
    private Long artist_id;
    @Column(name = "collection_id", nullable = true)
    private Long collection_id;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "artist_id", insertable = false, updatable = false)
    private ITunesArtist artist;
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "collection_id", insertable = false, updatable = false)
    private ITunesCollection collection;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created_at;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime modified_at;

    public ITunesTrack(ITunesProxyTrack track) {
        this.id = track.getTrackId();
        this.artist_id = track.getArtistId();
        this.collection_id = track.getCollectionId();
        this.name = track.getTrackName();
        this.censored_name = track.getTrackCensoredName();
        this.view_url = track.getTrackViewUrl();
        this.preview_url = track.getPreviewUrl();
        this.artwork_url_60 = track.getArtworkUrl60();
        this.artwork_url_100 = track.getArtworkUrl100();
        this.price = track.getTrackPrice();
        this.explicitness = track.getTrackExplicitness();
        this.disc_count = track.getDiscCount();
        this.disc_number = track.getDiscNumber();
        this.track_count = track.getTrackCount();
        this.track_number = track.getTrackNumber();
        this.track_time_millis = track.getTrackTimeMillis();
        this.is_streamable = track.getIsStreamable();
        this.country = track.getCountry();
        this.currency = track.getCurrency();
        this.release_date = Utils.parse_release_date(track.getReleaseDate());
        this.primary_genre_name = track.getPrimaryGenreName();
    }
}
