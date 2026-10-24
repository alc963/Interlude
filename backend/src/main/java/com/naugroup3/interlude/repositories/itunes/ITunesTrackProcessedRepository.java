package com.naugroup3.interlude.repositories.itunes;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naugroup3.interlude.models.itunes.ITunesTrackProcessed;

public interface ITunesTrackProcessedRepository extends JpaRepository<ITunesTrackProcessed, Long> {
}
