package com.naugroup3.interlude.repositories.spotify;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.naugroup3.interlude.models.spotify.SpotifyRecentlyPlayed;

public interface SpotifyRecentlyPlayedRepository extends JpaRepository<SpotifyRecentlyPlayed, Long> {
    @Query("SELECT MAX(p.played_at) FROM SpotifyRecentlyPlayed p WHERE p.user_id = ?1")
    Optional<Instant> find_latest_played_at(Long user_id);

    @Query("SELECT p.track_uri FROM SpotifyRecentlyPlayed p WHERE p.user_id = ?1 ORDER BY p.played_at DESC")
    List<String> find_recent_track_uris(Long user_id, Pageable page);

    interface TrackPlays {
        String getTrackUri();
        String getTrackName();
        String getArtistUri();
        String getArtistName();
        String getAlbumName();
        Integer getDurationMs();
        long getPlayCount();
    }

    interface ArtistPlays {
        String getArtistUri();
        String getArtistName();
        long getPlayCount();
        String getSampleTrackUri();
    }

    interface AlbumPlays {
        String getAlbumUri();
        String getAlbumName();
        String getArtistUri();
        String getArtistName();
        long getPlayCount();
        String getSampleTrackUri();
    }

    @Query("SELECT p.track_uri AS trackUri, MAX(p.track_name) AS trackName, MAX(p.artist_uri) AS artistUri, MAX(p.artist_name) AS artistName, MAX(p.album_name) AS albumName, MAX(p.duration_ms) AS durationMs, COUNT(p) AS playCount FROM SpotifyRecentlyPlayed p WHERE p.user_id = ?1 GROUP BY p.track_uri ORDER BY COUNT(p) DESC, MAX(p.played_at) DESC")
    List<TrackPlays> find_top_tracks(Long user_id, Pageable page);

    @Query("SELECT p.track_uri AS trackUri, MAX(p.track_name) AS trackName, MAX(p.artist_uri) AS artistUri, MAX(p.artist_name) AS artistName, MAX(p.album_name) AS albumName, MAX(p.duration_ms) AS durationMs, COUNT(p) AS playCount FROM SpotifyRecentlyPlayed p WHERE p.user_id = ?1 AND p.artist_uri = ?2 GROUP BY p.track_uri ORDER BY COUNT(p) DESC, MAX(p.played_at) DESC")
    List<TrackPlays> find_top_tracks_by_artist(Long user_id, String artist_uri, Pageable page);

    @Query("SELECT p.artist_uri AS artistUri, MAX(p.artist_name) AS artistName, COUNT(p) AS playCount, MAX(p.track_uri) AS sampleTrackUri FROM SpotifyRecentlyPlayed p WHERE p.user_id = ?1 AND p.artist_uri IS NOT NULL GROUP BY p.artist_uri ORDER BY COUNT(p) DESC, MAX(p.played_at) DESC")
    List<ArtistPlays> find_top_artists(Long user_id, Pageable page);

    @Query("SELECT p.album_uri AS albumUri, MAX(p.album_name) AS albumName, MAX(p.artist_uri) AS artistUri, MAX(p.artist_name) AS artistName, COUNT(p) AS playCount, MAX(p.track_uri) AS sampleTrackUri FROM SpotifyRecentlyPlayed p WHERE p.user_id = ?1 AND p.album_uri IS NOT NULL GROUP BY p.album_uri ORDER BY COUNT(p) DESC, MAX(p.played_at) DESC")
    List<AlbumPlays> find_top_albums(Long user_id, Pageable page);

    @Query("SELECT p.album_uri AS albumUri, MAX(p.album_name) AS albumName, MAX(p.artist_uri) AS artistUri, MAX(p.artist_name) AS artistName, COUNT(p) AS playCount, MAX(p.track_uri) AS sampleTrackUri FROM SpotifyRecentlyPlayed p WHERE p.user_id = ?1 AND p.artist_uri = ?2 AND p.album_uri IS NOT NULL GROUP BY p.album_uri ORDER BY COUNT(p) DESC, MAX(p.played_at) DESC")
    List<AlbumPlays> find_top_albums_by_artist(Long user_id, String artist_uri, Pageable page);
}
