package com.naugroup3.interlude.services.MusicAnalyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;

import com.naugroup3.interlude.models.itunes.ITunesTrack;
import com.naugroup3.interlude.models.itunes.ITunesTrackSampleMood;
import com.naugroup3.interlude.repositories.itunes.ITunesTrackRepository;
import com.naugroup3.interlude.repositories.itunes.ITunesTrackSampleMoodRepository;
import com.naugroup3.interlude.services.MusicAnalyzer.mood_detector.MoodDetector.MoodResult;
import com.naugroup3.interlude.services.MusicAnalyzer.vibesnet.Vibesnet.MusicAnalytics;

class MusicAnalyzerTest {
    private static final MoodResult MOOD_RESULT = new MoodResult("House/Dance", 0.4, 0.7, 124.0, "A major", Map.of(), "explanation");
    private static final MusicAnalytics ANALYTICS = new MusicAnalytics(0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7);

    @TempDir
    Path temp_dir;

    private ITunesTrackRepository track_repo;
    private ITunesTrackSampleMoodRepository sample_mood_repo;
    private MusicAnalyzer analyzer;

    @BeforeEach
    void set_up() {
        track_repo = mock(ITunesTrackRepository.class);
        sample_mood_repo = mock(ITunesTrackSampleMoodRepository.class);
        analyzer = analyzer(this::write_sample, path -> MOOD_RESULT, path -> ANALYTICS);
    }

    @AfterEach
    void tear_down() {
        analyzer.shutdown();
    }

    private MusicAnalyzer analyzer(Function<String, Path> download_sample, Function<String, MoodResult> detect_mood, Function<String, MusicAnalytics> analyze_vibes) {
        return new MusicAnalyzer(track_repo, sample_mood_repo, download_sample, detect_mood, analyze_vibes);
    }

    private Path write_sample(String url) {
        try {
            return Files.createTempFile(temp_dir, "sample", ".m4a");
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void wait_until(BooleanSupplier condition) throws InterruptedException {
        for(int attempt = 0; attempt < 200 && !condition.getAsBoolean(); attempt++) Thread.sleep(10);
    }

    private static ITunesTrack track(long track_id, String preview_url) {
        final ITunesTrack track = mock(ITunesTrack.class);
        when(track.getId()).thenReturn(track_id);
        when(track.getPreview_url()).thenReturn(preview_url);
        return track;
    }

    @Test
    void saves_combined_mood_and_vibes_for_enqueued_track() {
        analyzer.enqueue(track(7L, "https://example.com/sample.m4a"));

        final ArgumentCaptor<ITunesTrackSampleMood> saved_rows = ArgumentCaptor.forClass(ITunesTrackSampleMood.class);
        verify(sample_mood_repo, timeout(2000)).save(saved_rows.capture());
        final ITunesTrackSampleMood row = saved_rows.getValue();
        assertThat(row.getId()).isEqualTo(7L);
        assertThat(row.getMood()).isEqualTo("House/Dance");
        assertThat(row.getTempo()).isEqualTo(124.0);
        assertThat(row.getKey()).isEqualTo("A major");
        assertThat(row.getBrightness()).isEqualTo(0.7);
        // mood-detector energy
        assertThat(row.getEnergy()).isEqualTo(0.4);
        // vibesnet energy
        assertThat(row.getArousal()).isEqualTo(0.3);
        assertThat(row.getAcousticness()).isEqualTo(0.1);
        assertThat(row.getDanceability()).isEqualTo(0.2);
        assertThat(row.getInstrumentalness()).isEqualTo(0.4);
        assertThat(row.getLiveness()).isEqualTo(0.5);
        assertThat(row.getSpeechiness()).isEqualTo(0.6);
        assertThat(row.getValence()).isEqualTo(0.7);
    }

    @Test
    void ignores_tracks_without_a_preview() {
        analyzer.enqueue(track(1L, null));

        verify(sample_mood_repo, never()).existsById(any());
        verify(sample_mood_repo, never()).save(any());
    }

    @Test
    void skips_tracks_that_already_have_a_sample_mood() throws InterruptedException {
        when(sample_mood_repo.existsById(3L)).thenReturn(true);
        final CountDownLatch download_started = new CountDownLatch(1);
        analyzer = analyzer(url -> {
            download_started.countDown();
            return write_sample(url);
        }, path -> MOOD_RESULT, path -> ANALYTICS);

        analyzer.enqueue(track(3L, "https://example.com/sample.m4a"));

        verify(sample_mood_repo, timeout(2000)).existsById(3L);
        assertThat(download_started.await(200, TimeUnit.MILLISECONDS)).isFalse();
        verify(sample_mood_repo, never()).save(any());
    }

    @Test
    void deletes_the_downloaded_sample_even_when_analysis_fails() throws InterruptedException {
        final AtomicReference<Path> downloaded_sample = new AtomicReference<>();
        final CountDownLatch analysis_attempted = new CountDownLatch(1);
        analyzer = analyzer(url -> {
            downloaded_sample.set(write_sample(url));
            return downloaded_sample.get();
        }, path -> {
            analysis_attempted.countDown();
            throw new IllegalArgumentException("bad audio");
        }, path -> ANALYTICS);

        analyzer.enqueue(track(4L, "https://example.com/sample.m4a"));

        assertThat(analysis_attempted.await(2, TimeUnit.SECONDS)).isTrue();
        verify(sample_mood_repo, timeout(2000).times(0)).save(any());
        wait_until(() -> !Files.exists(downloaded_sample.get()));
        assertThat(downloaded_sample.get()).doesNotExist();
    }

    @Test
    void keeps_going_after_a_failed_track() {
        final AtomicInteger mood_attempts = new AtomicInteger();
        analyzer = analyzer(this::write_sample, path -> {
            if (mood_attempts.getAndIncrement() == 0) throw new IllegalStateException("boom");
            return MOOD_RESULT;
        }, path -> ANALYTICS);

        analyzer.enqueue(track(1L, "https://example.com/a.m4a"));
        analyzer.enqueue(track(2L, "https://example.com/b.m4a"));

        final ArgumentCaptor<ITunesTrackSampleMood> saved_rows = ArgumentCaptor.forClass(ITunesTrackSampleMood.class);
        verify(sample_mood_repo, timeout(2000)).save(saved_rows.capture());
        assertThat(saved_rows.getValue().getId()).isEqualTo(2L);
    }

    @Test
    void analyzes_one_track_at_a_time_in_order() throws InterruptedException {
        final AtomicInteger currently_analyzing = new AtomicInteger();
        final AtomicInteger max_concurrent_analyses = new AtomicInteger();
        final List<Long> download_order = new CopyOnWriteArrayList<>();
        analyzer = analyzer(url -> {
            download_order.add(Long.parseLong(url.substring(url.lastIndexOf('/') + 1, url.lastIndexOf('.'))));
            return write_sample(url);
        }, path -> {
            max_concurrent_analyses.accumulateAndGet(currently_analyzing.incrementAndGet(), Math::max);
            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            currently_analyzing.decrementAndGet();
            return MOOD_RESULT;
        }, path -> ANALYTICS);

        for (long track_id = 1; track_id <= 4; track_id++) analyzer.enqueue(track(track_id, "https://example.com/" + track_id + ".m4a"));

        verify(sample_mood_repo, timeout(5000).times(4)).save(any());
        assertThat(max_concurrent_analyses.get()).isEqualTo(1);
        assertThat(download_order).containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void ignores_a_track_that_is_already_queued() throws InterruptedException {
        final CountDownLatch release_analysis = new CountDownLatch(1);
        final CountDownLatch analysis_started = new CountDownLatch(1);
        analyzer = analyzer(this::write_sample, path -> {
            analysis_started.countDown();
            try {
                release_analysis.await(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return MOOD_RESULT;
        }, path -> ANALYTICS);

        final ITunesTrack track = track(9L, "https://example.com/sample.m4a");
        analyzer.enqueue(track);
        assertThat(analysis_started.await(2, TimeUnit.SECONDS)).isTrue();
        analyzer.enqueue(track);
        release_analysis.countDown();

        verify(sample_mood_repo, timeout(2000).times(1)).save(any());
        verify(sample_mood_repo, timeout(500).times(1)).existsById(9L);
    }

    @Test
    void startup_enqueues_every_unprocessed_track() {
        final List<ITunesTrack> unprocessed = List.of(
            track(1L, "https://example.com/a.m4a"),
            track(2L, "https://example.com/b.m4a"));
        when(track_repo.find_unprocessed_tracks()).thenReturn(unprocessed);

        analyzer.enqueue_unprocessed();

        verify(sample_mood_repo, timeout(2000).times(2)).save(any());
    }
}
