package com.naugroup3.interlude.repositories.itunes;

import org.springframework.data.jpa.repository.JpaRepository;

import com.naugroup3.interlude.models.itunes.ServiceIdToITunesTrack;

public interface ServiceIdToITunesTrackRepo extends JpaRepository<ServiceIdToITunesTrack, String>  {
    
}
