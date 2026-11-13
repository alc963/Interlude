package com.naugroup3.interlude.services.MusicAnalyzer.mood_detector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import com.naugroup3.interlude.services.MusicAnalyzer.AudioDecoder;

// ported from https://github.com/usr-wwelsh/Mood-Detector
public final class MoodDetector {
    public record MoodResult(String mood, double energy, double brightness, double tempo, String key, Map<String, Double> similarity_scores, String explanation) {}

    private MoodDetector() {}

    public static MoodResult analyze(String file_path) {
        final Path path = Path.of(file_path);
        if (!Files.exists(path)) {
            throw new IllegalArgumentException("Audio file not found: " + file_path);
        }
        final Features.Extracted features = Features.extract(AudioDecoder.decode_skip_intro(file_path, Features.SAMPLE_RATE));
        return MoodClassifier.classify(features);
    }
}
