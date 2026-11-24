package com.naugroup3.interlude.repositories.itunes;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.naugroup3.interlude.models.itunes.ServiceIdToITunesTrack;

public interface ServiceIdToITunesTrackRepo extends JpaRepository<ServiceIdToITunesTrack, String>  {
    interface ServiceIdArtwork {
        String getServiceId();
        String getArtworkUrl();
    }

    @Query("SELECT m.service_id AS serviceId, t.artwork_url_100 AS artworkUrl FROM ServiceIdToITunesTrack m JOIN ITunesTrack t ON t.id = m.track_id WHERE m.service_id IN ?1 AND t.artwork_url_100 IS NOT NULL")
    List<ServiceIdArtwork> find_artwork(Collection<String> service_ids);
}
