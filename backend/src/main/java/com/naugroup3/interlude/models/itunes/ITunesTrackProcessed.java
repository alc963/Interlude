package com.naugroup3.interlude.models.itunes;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

@Getter
@Entity
@Table(name = "itunes_track_processing")
public class ITunesTrackProcessed {
    @Id
    private Long id;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created_at;

    public ITunesTrackProcessed(Long track_id) {
        this.id = track_id;
    }
}
