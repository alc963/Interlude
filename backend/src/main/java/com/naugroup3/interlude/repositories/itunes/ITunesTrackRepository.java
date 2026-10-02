package com.naugroup3.interlude.repositories.itunes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naugroup3.interlude.models.itunes.ITunesTrack;

public interface ITunesTrackRepository extends JpaRepository<ITunesTrack, Long> {
    List<ITunesTrack> findByCollection_id(Long collection_id);
    Optional<ITunesTrack> findByCollection_idAndName(Long collection_id, String name);
}
