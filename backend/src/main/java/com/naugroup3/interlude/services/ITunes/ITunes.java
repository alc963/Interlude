package com.naugroup3.interlude.services.ITunes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.naugroup3.interlude.models.itunes.ITunesCollection;
import com.naugroup3.interlude.models.itunes.ITunesArtist;
import com.naugroup3.interlude.models.itunes.ITunesTrack;
import com.naugroup3.interlude.models.itunes.ServiceIdToITunesTrack;

public class ITunes {
    public static record GenericTrack(String service_uri, String title, String artist, String album, Long duration_milliseconds, Boolean explicit){}
    private final ITunesDatabase database;
    private final ITunesProxy proxy;

    private List<Consumer<ITunesTrack>> on_found_track_callbacks = new ArrayList<>();

    public ITunes(ITunesDatabase database) {
        this.database = database;
        this.proxy = new ITunesProxy(database);
    }

    public void add_on_found_track_callback(Consumer<ITunesTrack> callback){
        this.on_found_track_callbacks.add(callback);
    }

    public void process_track(GenericTrack track){
        Optional<ServiceIdToITunesTrack> maybe_id = this.database.service_id_to_itunes_id(track.service_uri());
        if(maybe_id.isPresent()) {
            if(database.is_track_processed(maybe_id.get().getTrack_id())) return;
        }
        find_track(track).thenAccept(found -> {
            if(found == null) return;

            database.insert_service_id_track_id_mapping(track.service_uri(), found.getId());
            if(database.is_track_processed(found.getId())) return;
            for(Consumer<ITunesTrack> on_found_track_callback : on_found_track_callbacks) {
                on_found_track_callback.accept(found);
            }
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
