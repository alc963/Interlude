package com.naugroup3.interlude.models.spotify;

import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "spotify_recently_played")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpotifyRecentlyPlayed {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long user_id;
    @Column(nullable = false)
    private String track_uri;
    @Column(length = 500, nullable = false)
    private String track_name;
    @Column(length = 500)
    private String artist_name;
    @Column(length = 500)
    private String album_name;
    private Integer duration_ms;
    @Column(nullable = false)
    private Instant played_at;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Instant created_at;

    public SpotifyRecentlyPlayed(Long user_id, SpotifyTrack track, Instant played_at) {
        this.user_id = user_id;
        this.track_uri = track.uri();
        this.track_name = track.name();
        this.artist_name = track.artists() == null || track.artists().isEmpty() ? null : track.artists().get(0).name();
        this.album_name = track.album() == null ? null : track.album().name();
        this.duration_ms = track.durationMs();
        this.played_at = played_at;
    }
}
