package com.naugroup3.interlude.services.ITunes;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.naugroup3.interlude.models.itunes.ITunesArtist;
import com.naugroup3.interlude.models.itunes.ITunesCollection;
import com.naugroup3.interlude.models.itunes.ITunesTrack;
import com.naugroup3.interlude.models.itunes.ServiceIdToITunesTrack;
import com.naugroup3.interlude.repositories.itunes.ITunesArtistRepository;
import com.naugroup3.interlude.repositories.itunes.ITunesCollectionRepository;
import com.naugroup3.interlude.repositories.itunes.ITunesTrackProcessedRepository;
import com.naugroup3.interlude.repositories.itunes.ITunesTrackRepository;
import com.naugroup3.interlude.repositories.itunes.ServiceIdToITunesTrackRepo;

@Service
public class ITunesDatabase {
    private ServiceIdToITunesTrackRepo service_id_to_itunes_track_repo;
    private ITunesTrackProcessedRepository track_processed_repo;
    private ITunesTrackRepository track_repo;
    private ITunesCollectionRepository collection_repo;
    private ITunesArtistRepository artist_repo;

    public ITunesDatabase(ITunesTrackRepository track_repo, ITunesCollectionRepository collection_repo, ITunesArtistRepository artist_repo) {
        this.track_repo = track_repo;
        this.collection_repo = collection_repo;
        this.artist_repo = artist_repo;
    }

    public void insert_artist(ITunesArtist artist) {
        this.artist_repo.save(artist);
    }
    public void insert_album(ITunesCollection collection) {
        this.collection_repo.save(collection);
    }
    public void insert_track(ITunesTrack track) { 
        this.track_repo.save(track);
    }

    public List<ITunesArtist> get_artists(String name) {
        return artist_repo.findByName(name);
    }

    public List<ITunesCollection> get_artist_albums(Long artist_id) {
        return collection_repo.findByArtist_id(artist_id);
    }

    public Optional<ITunesCollection> get_artist_album(Long artist_id, String album) {
        return collection_repo.findByArtist_idAndCleanedName(artist_id, album);
    }

    public List<ITunesTrack> get_album_tracks(Long collection_id) {
        return track_repo.findByCollection_id(collection_id);
    }

    public Optional<ITunesTrack> get_album_track(Long collection_id, String track) {
        return track_repo.findByCollection_idAndName(collection_id, track);
    }

    public Optional<ServiceIdToITunesTrack> service_id_to_itunes_id(String service_id) {
        return service_id_to_itunes_track_repo.findById(service_id);
    }

    public Boolean is_track_processed(Long track_id) {
        return track_processed_repo.existsById(track_id);
    }
}
