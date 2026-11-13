package com.naugroup3.interlude.models.itunes;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.naugroup3.interlude.services.MusicAnalyzer.mood_detector.MoodDetector.MoodResult;
import com.naugroup3.interlude.services.MusicAnalyzer.vibesnet.Vibesnet.MusicAnalytics;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "itunes_track_sample_mood")
public class ITunesTrackSampleMood {
    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String mood;
    @Column(nullable = false)
    private Double tempo;
    @Column(nullable = false)
    private Double brightness;
    @Column(nullable = false)
    private Double energy;
    @Column(name = "key", nullable = false, length = 20)
    private String key;

    @Column(nullable = false)
    private Double acousticness;
    @Column(nullable = false)
    private Double instrumentalness;
    @Column(nullable = false)
    private Double liveness;
    @Column(nullable = false)
    private Double danceability;
    @Column(nullable = false)
    private Double arousal;
    @Column(nullable = false)
    private Double valence;
    @Column(nullable = false)
    private Double speechiness;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created_at;

    protected ITunesTrackSampleMood() {}

    public ITunesTrackSampleMood(Long track_id, MoodResult mood, MusicAnalytics analytics) {
        this.id = track_id;
        this.mood = mood.mood();
        this.tempo = mood.tempo();
        this.brightness = mood.brightness();
        this.energy = mood.energy();
        this.key = mood.key();
        this.acousticness = analytics.acousticness();
        this.instrumentalness = analytics.instrumentalness();
        this.liveness = analytics.liveness();
        this.danceability = analytics.danceability();
        this.arousal = analytics.energy();
        this.valence = analytics.valence();
        this.speechiness = analytics.speechiness();
    }
}
