package com.naugroup3.interlude.models.itunes;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import com.naugroup3.interlude.utils.Utils;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "itunes_collection")
public final class ITunesCollection {
    @Id
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;
    @Column(nullable = true, length = 200)
    private String censored_name;
    @Column(unique = true, nullable = false, length = 200)
    private String view_url;

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

    @Column(nullable = true, length = 500)
    private String copyright;
    @Column(nullable = true, length = 20)
    private String content_advisory_rating;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "artist_id", insertable = false, updatable = false,
        foreignKey = @ForeignKey(ConstraintMode.NO_CONSTRAINT))
    private ITunesArtist artist;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created_at;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime modified_at;

    public ITunesCollection(ITunesProxyCollection collection) {
        this.id = collection.getCollectionId();
        this.artist_id = collection.getArtistId();
        this.name = collection.getCollectionName();
        this.censored_name = collection.getCollectionCensoredName();
        this.view_url = collection.getCollectionViewUrl();
        this.artwork_url_60 = collection.getArtworkUrl60();
        this.artwork_url_100 = collection.getArtworkUrl100();
        this.price = collection.getCollectionPrice();
        this.explicitness = collection.getCollectionExplicitness();
        this.content_advisory_rating = collection.getContentAdvisoryRating();
        this.disc_count = collection.getDiscCount();
        this.disc_number = collection.getDiscNumber();
        this.track_count = collection.getTrackCount();
        this.copyright = collection.getCopyright();
        this.country = collection.getCountry();
        this.currency = collection.getCurrency();
        this.release_date = Utils.parse_release_date(collection.getReleaseDate());
        this.primary_genre_name = collection.getPrimaryGenreName();
    }

    public String get_cleaned_name() {
        return this.name.replace(" - Single", "").replace(" - EP", "").replace(" - Album", "");
    }
}
