package com.naugroup3.interlude.services.MusicAnalyzer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.naugroup3.interlude.models.itunes.ITunesTrack;
import com.naugroup3.interlude.models.itunes.ITunesTrackSampleMood;
import com.naugroup3.interlude.repositories.itunes.ITunesTrackRepository;
import com.naugroup3.interlude.repositories.itunes.ITunesTrackSampleMoodRepository;
import com.naugroup3.interlude.services.MusicAnalyzer.mood_detector.MoodDetector;
import com.naugroup3.interlude.services.MusicAnalyzer.mood_detector.MoodDetector.MoodResult;
import com.naugroup3.interlude.services.MusicAnalyzer.vibesnet.Vibesnet;
import com.naugroup3.interlude.services.MusicAnalyzer.vibesnet.Vibesnet.MusicAnalytics;
import com.naugroup3.interlude.utils.Fetch;

import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class MusicAnalyzer {
    private final ExecutorService worker = Executors.newSingleThreadExecutor(runnable -> {
        final Thread thread = new Thread(runnable, "music-analyzer");
        thread.setDaemon(true);
        return thread;
    });
    private final Set<Long> pending = ConcurrentHashMap.newKeySet();

    private final ITunesTrackRepository track_repo;
    private final ITunesTrackSampleMoodRepository sample_mood_repo;
    private final Function<String, Path> download;
    private final Function<String, MoodResult> detect_mood;
    private final Function<String, MusicAnalytics> analyze_vibes;

    @Autowired
    public MusicAnalyzer(ITunesTrackRepository track_repo, ITunesTrackSampleMoodRepository sample_mood_repo) {
        this(track_repo, sample_mood_repo, MusicAnalyzer::download_sample, MoodDetector::analyze, Vibesnet::analyze);
    }

    // constructor for mock testing
    MusicAnalyzer(ITunesTrackRepository track_repo, ITunesTrackSampleMoodRepository sample_mood_repo,
            Function<String, Path> download, Function<String, MoodResult> detect_mood, Function<String, MusicAnalytics> analyze_vibes) {
        this.track_repo = track_repo;
        this.sample_mood_repo = sample_mood_repo;
        this.download = download;
        this.detect_mood = detect_mood;
        this.analyze_vibes = analyze_vibes;
    }

    public void enqueue(ITunesTrack track) {
        if (track.getPreview_url() == null) return;
        final Long id = track.getId();
        if (!pending.add(id)) return;

        worker.execute(() -> {
            try {
                analyze(track);
            } catch (Exception e) {
                log.error("Failed to analyze iTunes track {}", id, e);
            } finally {
                pending.remove(id);
            }
        });
    }

    @EventListener(ApplicationReadyEvent.class)
    void enqueue_unprocessed() {
        final List<ITunesTrack> tracks = track_repo.find_unprocessed_tracks();
        log.info("Enqueueing {} unprocessed iTunes tracks", tracks.size());
        tracks.forEach(this::enqueue);
    }

    @PreDestroy
    void shutdown() {
        worker.shutdownNow();
    }

    private void analyze(ITunesTrack track) throws IOException {
        if (sample_mood_repo.existsById(track.getId())) return;

        final Path sample = download.apply(track.getPreview_url());
        try {
            final MoodResult mood = detect_mood.apply(sample.toString());
            final MusicAnalytics analytics = analyze_vibes.apply(sample.toString());
            sample_mood_repo.save(new ITunesTrackSampleMood(track.getId(), mood, analytics));
        } finally {
            Files.deleteIfExists(sample);
        }
    }

    private static Path download_sample(String url) {
        try {
            final Path file = Files.createTempFile("itunes-sample-", extension(url));
            final HttpResponse<Path> response = Fetch.download(url, file);
            if (response == null || response.statusCode() != 200) {
                Files.deleteIfExists(file);
                throw new IllegalStateException("Could not download sample " + url);
            }
            return file;
        } catch (IOException e) {
            throw new IllegalStateException("Could not store sample " + url, e);
        }
    }

    private static String extension(String url) {
        final String path = URI.create(url).getPath();
        if(path == null) return "";
        final int dot = path == null ? -1 : path.lastIndexOf('.');
        return dot < 0 ? ".m4a" : path.substring(dot);
    }
}
