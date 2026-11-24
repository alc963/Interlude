package com.naugroup3.interlude.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.naugroup3.interlude.models.top.TopAlbum;
import com.naugroup3.interlude.models.top.TopArtist;
import com.naugroup3.interlude.models.top.TopArtistSummary;
import com.naugroup3.interlude.models.top.TopArtistRef;
import com.naugroup3.interlude.models.top.TopTrack;
import com.naugroup3.interlude.repositories.itunes.ServiceIdToITunesTrackRepo;
import com.naugroup3.interlude.repositories.itunes.ServiceIdToITunesTrackRepo.ServiceIdArtwork;
import com.naugroup3.interlude.repositories.spotify.SpotifyRecentlyPlayedRepository;
import com.naugroup3.interlude.repositories.spotify.SpotifyRecentlyPlayedRepository.AlbumPlays;
import com.naugroup3.interlude.repositories.spotify.SpotifyRecentlyPlayedRepository.ArtistPlays;
import com.naugroup3.interlude.repositories.spotify.SpotifyRecentlyPlayedRepository.TrackPlays;

@Service
public class TopItemsService {
    private final SpotifyRecentlyPlayedRepository recently_played_repo;
    private final ServiceIdToITunesTrackRepo service_id_repo;

    public TopItemsService(SpotifyRecentlyPlayedRepository recently_played_repo, ServiceIdToITunesTrackRepo service_id_repo) {
        this.recently_played_repo = recently_played_repo;
        this.service_id_repo = service_id_repo;
    }

    @Transactional(readOnly = true)
    public List<TopTrack> top_tracks(Long user_id, int limit) {
        return to_top_tracks(recently_played_repo.find_top_tracks(user_id, PageRequest.of(0, limit)));
    }

    @Transactional(readOnly = true)
    public List<TopArtist> top_artists(Long user_id, int limit) {
        final List<ArtistPlays> plays = recently_played_repo.find_top_artists(user_id, PageRequest.of(0, limit));
        final Map<String, String> artwork = artwork_by_uri(plays.stream().map(ArtistPlays::getSampleTrackUri).toList());
        return plays.stream()
            .map(p -> new TopArtist(p.getArtistUri(), p.getArtistName(), artwork.get(p.getSampleTrackUri()), p.getPlayCount()))
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TopAlbum> top_albums(Long user_id, int limit) {
        return to_top_albums(recently_played_repo.find_top_albums(user_id, PageRequest.of(0, limit)));
    }

    @Transactional(readOnly = true)
    public TopArtistSummary top_artist_summary(Long user_id, String artist_uri, int limit) {
        return new TopArtistSummary(
            to_top_tracks(recently_played_repo.find_top_tracks_by_artist(user_id, artist_uri, PageRequest.of(0, limit))),
            to_top_albums(recently_played_repo.find_top_albums_by_artist(user_id, artist_uri, PageRequest.of(0, limit))));
    }

    private List<TopTrack> to_top_tracks(List<TrackPlays> plays) {
        final Map<String, String> artwork = artwork_by_uri(plays.stream().map(TrackPlays::getTrackUri).toList());
        final List<TopTrack> tracks = new ArrayList<>();
        for(TrackPlays p : plays) {
            tracks.add(new TopTrack(
                p.getTrackUri(),
                p.getTrackName(),
                artist_refs(p.getArtistUri(), p.getArtistName()),
                p.getAlbumName(),
                p.getDurationMs() == null ? null : p.getDurationMs() / 1000,
                p.getPlayCount(),
                tracks.size() + 1,
                artwork.get(p.getTrackUri())));
        }
        return tracks;
    }

    private List<TopAlbum> to_top_albums(List<AlbumPlays> plays) {
        final Map<String, String> artwork = artwork_by_uri(plays.stream().map(AlbumPlays::getSampleTrackUri).toList());
        return plays.stream()
            .map(p -> new TopAlbum(p.getAlbumUri(), p.getAlbumName(), artist_refs(p.getArtistUri(), p.getArtistName()),
                artwork.get(p.getSampleTrackUri()), p.getPlayCount()))
            .toList();
    }

    private static List<TopArtistRef> artist_refs(String artist_uri, String artist_name) {
        return artist_name == null ? List.of() : List.of(new TopArtistRef(artist_uri, artist_name));
    }

    private Map<String, String> artwork_by_uri(List<String> uris) {
        if(uris.isEmpty()) return Map.of();
        return service_id_repo.find_artwork(uris).stream()
            .collect(Collectors.toMap(ServiceIdArtwork::getServiceId, ServiceIdArtwork::getArtworkUrl));
    }
}
