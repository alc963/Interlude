package com.naugroup3.interlude.repositories.itunes;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.naugroup3.interlude.models.itunes.ITunesCollection;

public interface ITunesCollectionRepository extends JpaRepository<ITunesCollection, Long>  {
    List<ITunesCollection> findByArtist_id(Long artist_id);

    @Query("SELECT c FROM ITunesCollection c WHERE c.artist_id = ?1 AND REPLACE(REPLACE(REPLACE(c.name, ' - Single', ''), ' - EP', ''), ' - Album', '') = ?2")
    Optional<ITunesCollection> findByArtist_idAndCleanedName(Long artist_id, String name);
}
