package com.naugroup3.interlude.repositories.itunes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naugroup3.interlude.models.itunes.ITunesCollection;

public interface ITunesCollectionRepository extends JpaRepository<ITunesCollection, Long>  {
    List<ITunesCollection> findByArtist_id(Long artist_id);
}
