package com.naugroup3.interlude.models.spotify;

import java.time.Instant;

import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "spotify_currently_playing")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SpotifyCurrentlyPlaying {
    @Id
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
    private Integer progress_ms;
    @Column(nullable = false)
    private boolean is_playing;

    @UpdateTimestamp
    @Column(nullable = false)
    private Instant modified_at;

    public SpotifyCurrentlyPlaying(Long user_id, SpotifyTrack track, Integer progress_ms, boolean is_playing) {
        this.user_id = user_id;
        this.track_uri = track.uri();
        this.track_name = track.name();
        this.artist_name = track.artists() == null || track.artists().isEmpty() ? null : track.artists().get(0).name();
        this.album_name = track.album() == null ? null : track.album().name();
        this.duration_ms = track.durationMs();
        this.progress_ms = progress_ms;
        this.is_playing = is_playing;
    }
}
