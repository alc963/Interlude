package com.naugroup3.interlude.repositories.itunes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naugroup3.interlude.models.itunes.ITunesCollection;

public interface ITunesCollectionRepository extends JpaRepository<ITunesCollection, Long>  {
    List<ITunesCollection> findByArtist_id(Long artist_id);
    Optional<ITunesCollection> findByArtist_idAndName(Long artist_id, String name);
}
