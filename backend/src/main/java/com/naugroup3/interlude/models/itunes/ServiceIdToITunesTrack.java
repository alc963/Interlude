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
@Table(name = "itunes_service_id_to_itunes_track")
public class ServiceIdToITunesTrack {
    @Id
    private String service_id;

    @Column(nullable = false)
    private Long track_id;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private LocalDateTime created_at;

    public ServiceIdToITunesTrack(String service_id, Long track_id) {
        this.service_id = service_id;
        this.track_id = track_id;
    }
}
