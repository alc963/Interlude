package com.naugroup3.interlude.repositories.itunes;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naugroup3.interlude.models.itunes.ITunesArtist;

public interface ITunesArtistRepository extends JpaRepository<ITunesArtist, Long>  {
    List<ITunesArtist> findByName(String name);
}
