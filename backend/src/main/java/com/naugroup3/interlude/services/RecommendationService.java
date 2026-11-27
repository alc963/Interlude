package com.naugroup3.interlude.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.naugroup3.interlude.models.RecommendedTrack;
import com.naugroup3.interlude.models.itunes.ITunesTrack;
import com.naugroup3.interlude.models.itunes.ITunesTrackSampleMood;
import com.naugroup3.interlude.repositories.itunes.ITunesTrackRepository;
import com.naugroup3.interlude.repositories.itunes.ServiceIdToITunesTrackRepo;
import com.naugroup3.interlude.repositories.spotify.SpotifyCurrentlyPlayingRepository;
import com.naugroup3.interlude.repositories.spotify.SpotifyRecentlyPlayedRepository;

@Service
public class RecommendationService {
    private static final int HISTORY_SIZE = 200;
    private static final int CURRENTLY_PLAYING_WEIGHT = 3;
    private static final double MAX_TEMPO = 300.0;

    private static final double GENRE_WEIGHT = 0.5;
    private static final double MOOD_WEIGHT = 0.15;
    private static final double FEATURE_WEIGHT = 0.35;

    private record Analyzed(ITunesTrack track, ITunesTrackSampleMood mood) {}
    private record Scored(Analyzed analyzed, double score) {}

    private final SpotifyRecentlyPlayedRepository recently_played_repo;
    private final SpotifyCurrentlyPlayingRepository currently_playing_repo;
    private final ServiceIdToITunesTrackRepo service_id_repo;
    private final ITunesTrackRepository track_repo;

    public RecommendationService(SpotifyRecentlyPlayedRepository recently_played_repo, SpotifyCurrentlyPlayingRepository currently_playing_repo,
            ServiceIdToITunesTrackRepo service_id_repo, ITunesTrackRepository track_repo) {
        this.recently_played_repo = recently_played_repo;
        this.currently_playing_repo = currently_playing_repo;
        this.service_id_repo = service_id_repo;
        this.track_repo = track_repo;
    }

    @Transactional(readOnly = true)
    public List<RecommendedTrack> recommend(Long user_id, int limit) {
        final List<String> seed_uris = new ArrayList<>(
            recently_played_repo.find_recent_track_uris(user_id, PageRequest.of(0, HISTORY_SIZE))
        );

        currently_playing_repo.findById(user_id).ifPresent(now -> {
            for(int i = 0; i < CURRENTLY_PLAYING_WEIGHT; i++) seed_uris.add(now.getTrack_uri());
        });
        if(seed_uris.isEmpty()) return List.of();

        final Map<String, Long> uri_to_track_id = new HashMap<>();
        service_id_repo.findAllById(new HashSet<>(seed_uris))
            .forEach(mapping -> uri_to_track_id.put(mapping.getService_id(), mapping.getTrack_id()));
        if(uri_to_track_id.isEmpty()) return List.of();

        final Map<Long, Analyzed> analyzed_seeds = new HashMap<>();
        for(Object[] row : track_repo.find_analyzed_tracks(uri_to_track_id.values()))
            analyzed_seeds.put(((ITunesTrack)row[0]).getId(), new Analyzed((ITunesTrack)row[0], (ITunesTrackSampleMood)row[1]));

        final List<Analyzed> seeds = seed_uris.stream()
            .map(uri_to_track_id::get)
            .filter(id -> id != null && analyzed_seeds.containsKey(id))
            .map(analyzed_seeds::get)
            .toList();
        if(seeds.isEmpty()) return List.of();

        final Map<String, Double> genre_share = fractional_share_of_mapping(seeds.stream().map(s -> s.track().getPrimary_genre_name()).toList());
        final Map<String, Double> mood_share = fractional_share_of_mapping(seeds.stream().map(s -> s.mood().getMood()).toList());
        final double[] taste = average_vectors(seeds.stream().map(s -> normalized_features(s.mood())).toList());

        // filter off listened to music
        final Set<Long> listened = new HashSet<>(uri_to_track_id.values());
        return track_repo.find_analyzed_tracks_excluding(listened).stream()
            .map(row -> new Analyzed((ITunesTrack)row[0], (ITunesTrackSampleMood)row[1]))
            .map(candidate -> new Scored(candidate,
                GENRE_WEIGHT * genre_share.getOrDefault(candidate.track().getPrimary_genre_name(), 0.0)
                + MOOD_WEIGHT * mood_share.getOrDefault(candidate.mood().getMood(), 0.0)
                + FEATURE_WEIGHT * similarity(taste, normalized_features(candidate.mood()))))
            .sorted(Comparator.comparingDouble(Scored::score).reversed())
            .limit(limit)
            .map(RecommendationService::to_recommended_track)
            .toList();
    }

    private static Map<String, Double> fractional_share_of_mapping(List<String> values) {
        final Map<String, Double> share = new HashMap<>();
        values.stream().filter(v -> v != null).forEach(v -> share.merge(v, 1.0 / values.size(), Double::sum));
        return share;
    }

    private static double[] normalized_features(ITunesTrackSampleMood mood) {
        return new double[] {
            mood.getValence(),
            mood.getArousal(),
            mood.getDanceability(),
            mood.getAcousticness(),
            mood.getInstrumentalness(),
            clamp_01(mood.getEnergy()),
            clamp_01(mood.getBrightness()),
            clamp_01(mood.getTempo() / MAX_TEMPO),
        };
    }

    private static double[] average_vectors(List<double[]> vectors) {
        final double[] sum = new double[vectors.get(0).length];
        for(double[] vector : vectors) {
            for(int i = 0; i < sum.length; i++) sum[i] += vector[i];
        }
        for(int i = 0; i < sum.length; i++) sum[i] /= vectors.size();
        return sum;
    }

    // 1 = identical; 0 = different
    private static double similarity(double[] vec_a, double[] vec_b) {
        double distance_squared = 0;
        for(int i = 0; i < vec_a.length; i++) distance_squared += (vec_a[i] - vec_b[i]) * (vec_a[i] - vec_b[i]);
        return 1.0 - Math.sqrt(distance_squared / vec_a.length);
    }

    private static double clamp_01(double value) {
        return Math.max(0.0, Math.min(1.0, value));
    }

    private static RecommendedTrack to_recommended_track(Scored scored) {
        final ITunesTrack track = scored.analyzed().track();
        return new RecommendedTrack(
            track.getId(),
            track.getName(),
            track.getArtist() == null ? null : track.getArtist().getName(),
            track.getCollection() == null ? null : track.getCollection().get_cleaned_name(),
            track.getPrimary_genre_name(),
            scored.analyzed().mood().getMood(),
            track.getArtwork_url_100(),
            track.getPreview_url(),
            track.getView_url(),
            scored.score());
    }
}
