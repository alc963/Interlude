package com.naugroup3.interlude.services.ITunes;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.naugroup3.interlude.models.itunes.ITunesCollection;
import com.naugroup3.interlude.models.itunes.ITunesArtist;
import com.naugroup3.interlude.models.itunes.ITunesTrack;
import com.naugroup3.interlude.models.itunes.ServiceIdToITunesTrack;
import com.naugroup3.interlude.models.spotify.SpotifyTrack;
import com.naugroup3.interlude.models.spotify.SpotifyTracksFetchedEvent;
import com.naugroup3.interlude.services.MusicAnalyzer.MusicAnalyzer;

@Service
public class ITunes {
    public static record GenericTrack(String service_uri, String title, String artist, String album, Long duration_milliseconds, Boolean explicit){}
    private final ITunesDatabase database;
    private final ITunesProxy proxy;
    private final MusicAnalyzer analyzer;

    public ITunes(ITunesDatabase database, MusicAnalyzer analyzer) {
        this.database = database;
        this.proxy = new ITunesProxy(database);
        this.analyzer = analyzer;
    }

    public void enqueue_all_unprocessed_tracks() {
        this.database.unprocessed_track().forEach(analyzer::enqueue);
    }

    @EventListener
    public void on_spotify_tracks_fetched(SpotifyTracksFetchedEvent event) {
        event.tracks().forEach(track -> process_track(to_generic_track(track)));
    }

    private static GenericTrack to_generic_track(SpotifyTrack track) {
        final String artist = track.artists() == null || track.artists().isEmpty() ? null : track.artists().get(0).name();
        final String album = track.album() == null ? null : track.album().name();
        final Long duration = track.durationMs() == null ? null : track.durationMs().longValue();
        return new GenericTrack(track.uri(), track.name(), artist, album, duration, track.explicit());
    }

    public void process_track(GenericTrack track){
        Optional<ServiceIdToITunesTrack> maybe_id = this.database.service_id_to_itunes_id(track.service_uri());
        if(maybe_id.isPresent()) {
            if(database.is_track_processed(maybe_id.get().getTrack_id())) return;
        }
        find_track(track).thenAccept(found -> {
            if(found == null) return;

            database.save_service_id_track_id_mapping(track.service_uri(), found.getId());
            if(database.is_track_processed(found.getId())) return;
            analyzer.enqueue(found);
        });
    }

    private CompletableFuture<ITunesTrack> find_track(GenericTrack track) {
        return proxy.search_artists(track.artist())
            .thenCompose(artists_result -> {
                final List<ITunesArtist> artists = artists_result.has_value() ? artists_result.value() : List.of();
                return find_album(artists, 0, track);
            })
            .thenCompose(album -> {
                if(album == null) return CompletableFuture.completedFuture(null);
                return proxy.album_tracks(album.getId()).thenApply(tracks_result -> {
                    final List<ITunesTrack> tracks = tracks_result.has_value() ? tracks_result.value() : List.of();
                    return match_track(tracks, track);
                });
            });
    }

    private CompletableFuture<ITunesCollection> find_album(List<ITunesArtist> artists, int index, GenericTrack track) {
        if(index >= artists.size()) return CompletableFuture.completedFuture(null);
        final ITunesArtist artist = artists.get(index);
        return proxy.artists_albums(artist.getId()).thenCompose(albums_result -> {
            final List<ITunesCollection> albums = albums_result.has_value() ? albums_result.value() : List.of();
            final ITunesCollection match = match_album(albums, track);
            if(match != null) return CompletableFuture.completedFuture(match);
            return find_album(artists, index + 1, track);
        });
    }

    private ITunesCollection match_album(List<ITunesCollection> albums, GenericTrack track) {
        return albums.stream()
            .filter(a -> a.get_cleaned_name() != null && a.get_cleaned_name().equalsIgnoreCase(track.album()))
            .findFirst().orElse(null);
    }
    private ITunesTrack match_track(List<ITunesTrack> tracks, GenericTrack track) {
        return tracks.stream()
            .filter(t -> t.getName() != null && t.getName().equalsIgnoreCase(track.title()))
            .findFirst().orElse(null);
    }
}
