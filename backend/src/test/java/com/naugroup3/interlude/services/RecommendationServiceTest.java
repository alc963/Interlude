package com.naugroup3.interlude.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import com.naugroup3.interlude.models.RecommendedTrack;
import com.naugroup3.interlude.models.itunes.ITunesTrack;
import com.naugroup3.interlude.models.itunes.ITunesTrackSampleMood;
import com.naugroup3.interlude.models.itunes.ServiceIdToITunesTrack;
import com.naugroup3.interlude.repositories.itunes.ITunesTrackRepository;
import com.naugroup3.interlude.repositories.itunes.ServiceIdToITunesTrackRepo;
import com.naugroup3.interlude.repositories.spotify.SpotifyCurrentlyPlayingRepository;
import com.naugroup3.interlude.repositories.spotify.SpotifyRecentlyPlayedRepository;
import com.naugroup3.interlude.services.MusicAnalyzer.mood_detector.MoodDetector.MoodResult;
import com.naugroup3.interlude.services.MusicAnalyzer.vibesnet.Vibesnet.MusicAnalytics;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTest {
    @Mock private SpotifyRecentlyPlayedRepository recently_played_repo;
    @Mock private SpotifyCurrentlyPlayingRepository currently_playing_repo;
    @Mock private ServiceIdToITunesTrackRepo service_id_repo;
    @Mock private ITunesTrackRepository track_repo;
    @InjectMocks private RecommendationService service;

    private static Object[] analyzed(long id, String genre, String mood, double level) {
        final ITunesTrack track = mock(ITunesTrack.class);
        when(track.getId()).thenReturn(id);
        when(track.getPrimary_genre_name()).thenReturn(genre);
        final MoodResult mood_result = new MoodResult(mood, level, level, level * 250, "C", Map.of(), "");
        final MusicAnalytics analytics = new MusicAnalytics(level, level, level, level, level, level, level);
        return new Object[] { track, new ITunesTrackSampleMood(id, mood_result, analytics) };
    }

    @Test
    void returns_empty_without_history() {
        when(recently_played_repo.find_recent_track_uris(eq(7L), any(Pageable.class))).thenReturn(List.of());
        when(currently_playing_repo.findById(7L)).thenReturn(Optional.empty());
        assertThat(service.recommend(7L, 10)).isEmpty();
    }

    @Test
    void ranks_similar_genre_and_mood_first() {
        final List<Object[]> seeds = List.<Object[]>of(analyzed(1L, "Rock", "Energetic", 0.9));
        final List<Object[]> candidates = List.of(
            analyzed(2L, "Classical", "Calm", 0.1),
            analyzed(3L, "Rock", "Energetic", 0.85),
            analyzed(4L, "Rock", "Calm", 0.3));
        when(recently_played_repo.find_recent_track_uris(eq(7L), any(Pageable.class))).thenReturn(List.of("spotify:track:a"));
        when(currently_playing_repo.findById(7L)).thenReturn(Optional.empty());
        when(service_id_repo.findAllById(any())).thenReturn(List.of(new ServiceIdToITunesTrack("spotify:track:a", 1L)));
        when(track_repo.find_analyzed_tracks(anyCollection())).thenReturn(seeds);
        when(track_repo.find_analyzed_tracks_excluding(anyCollection())).thenReturn(candidates);

        final List<RecommendedTrack> recommendations = service.recommend(7L, 10);

        assertThat(recommendations).extracting(RecommendedTrack::id).containsExactly(3L, 4L, 2L);
    }
}
