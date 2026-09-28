package com.naugroup3.interlude.services.ITunes;

import java.util.List;

import org.springframework.stereotype.Service;

import com.naugroup3.interlude.models.itunes.ITunesArtist;
import com.naugroup3.interlude.models.itunes.ITunesCollection;
import com.naugroup3.interlude.models.itunes.ITunesTrack;
import com.naugroup3.interlude.repositories.itunes.ITunesArtistRepository;
import com.naugroup3.interlude.repositories.itunes.ITunesCollectionRepository;
import com.naugroup3.interlude.repositories.itunes.ITunesTrackRepository;

@Service
public class ITunesDatabase {
    private ITunesTrackRepository track_repo;
    private ITunesCollectionRepository collection_repo;
    private ITunesArtistRepository artist_repo;

    public ITunesDatabase(ITunesTrackRepository track_repo, ITunesCollectionRepository collection_repo, ITunesArtistRepository artist_repo) {
        this.track_repo = track_repo;
        this.collection_repo = collection_repo;
        this.artist_repo = artist_repo;
    }

    public List<ITunesArtist> get_artists(String name) {
        return artist_repo.findByName(name);
    }

    public List<ITunesCollection> get_artist_albums(Long artist_id) {
        return collection_repo.findByArtist_id(artist_id);
    }

    public List<ITunesTrack> get_album_tracks(Long collection_id) {
        return track_repo.findByCollection_id(collection_id);
    }
}
