package com.naugroup3.interlude.models.itunes;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "itunes_artist")
public final class ITunesArtist {
    @Id
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(unique = true, nullable = false, length = 200)
    private String link_url;
    @Column(nullable = true)
    private Long amg_artist_id;
    
    @Column(nullable = true, length = 80)
    private String primary_genre_name;
    @Column(nullable = true)
    private Long primary_genre_id;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created_at;
    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime modified_at;

    public ITunesArtist(ITunesProxyArtist proxy) {
        this.id = proxy.getArtistId();
        this.name = proxy.getArtistName();
        this.link_url = proxy.getArtistLinkUrl();
        this.amg_artist_id = proxy.getAmgArtistId();
        this.primary_genre_name = proxy.getPrimaryGenreName();
        this.primary_genre_id = proxy.getPrimaryGenreId();
    }
}
