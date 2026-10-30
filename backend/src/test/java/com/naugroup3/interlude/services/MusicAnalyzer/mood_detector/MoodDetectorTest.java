package com.naugroup3.interlude.services.MusicAnalyzer.mood_detector;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MoodDetectorTest {
    private static final int SAMPLE_RATE = 22050;

    @TempDir
    Path temp_dir;

    private static Features.Extracted features(double tempo, double tempo_confidence, double energy, double spectral_centroid, double zero_crossing_rate, double[] chroma) {
        return new Features.Extracted(tempo, tempo_confidence, energy, spectral_centroid, zero_crossing_rate, chroma, 30.0);
    }

    private static double[] chroma_with_peak(int root, int third) {
        final double[] chroma = new double[12];
        chroma[root] = 1.0;
        chroma[third] = 0.5;
        return chroma;
    }

    private static String mood(double tempo, double tempo_confidence, double energy, double spectral_centroid, double zero_crossing_rate, double[] chroma) {
        return MoodClassifier.classify(features(tempo, tempo_confidence, energy, spectral_centroid, zero_crossing_rate, chroma)).mood();
    }

    @Test
    void classifies_moods_by_feature_ranges() {
        final double[] major = chroma_with_peak(0, 4), minor = chroma_with_peak(0, 3);

        assertThat(mood(60, 0.0, 0.05, 1000, 0.05, major)).isEqualTo("Ambient/Drone");
        assertThat(mood(120, 1.0, 0.5, 4000, 0.3, major)).isEqualTo("Harsh Noise/Experimental");
        assertThat(mood(120, 1.0, 0.2, 4000, 0.3, major)).isEqualTo("Glitch/Experimental");
        assertThat(mood(130, 1.0, 0.6, 1000, 0.1, major)).isEqualTo("Techno/Dark");
        assertThat(mood(130, 1.0, 0.6, 3000, 0.1, major)).isEqualTo("Techno/Industrial");
        assertThat(mood(124, 1.0, 0.3, 3500, 0.1, major)).isEqualTo("House/Dance");
        assertThat(mood(124, 1.0, 0.3, 3500, 0.1, minor)).isEqualTo("Dark House");
    }

    @Test
    void reports_tempo_key_explanation_and_similarity_scores() {
        final var doubled = MoodClassifier.classify(features(90, 1.0, 0.9, 3000, 0.1, chroma_with_peak(0, 4)));
        assertThat(doubled.tempo()).isEqualTo(180.0);
        assertThat(doubled.mood()).isEqualTo("Drum & Bass");

        final var house = MoodClassifier.classify(features(124, 1.0, 0.3, 3500, 0.1, chroma_with_peak(9, 1)));
        assertThat(house.key()).isEqualTo("A major");
        assertThat(house.explanation()).isEqualTo("Moderate energy (0.300), dance tempo (124.0 BPM), bright/sharp timbre, key of A major");
        assertThat(house.similarity_scores()).containsOnlyKeys("House", "Techno", "Disco", "Ambient", "DnB", "Downtempo");
    }

    @Test
    void rejects_missing_files_and_unsupported_extensions() throws IOException {
        final Path text_file = Files.writeString(temp_dir.resolve("notes.txt"), "hello");

        assertThatThrownBy(() -> MoodDetector.analyze(temp_dir.resolve("nope.mp3").toString())).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> MoodDetector.analyze(text_file.toString())).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void detects_tempo_of_a_synthetic_track() throws IOException, InterruptedException {
        assumeTrue(ffmpeg_available(), "ffmpeg is not installed");

        final int sample_count = SAMPLE_RATE * 40;
        final double[] waveform = new double[sample_count];
        for (int sample = 0; sample < sample_count; sample++) {
            final double seconds = (double) sample / SAMPLE_RATE;
            waveform[sample] = 0.1 * (Math.sin(2 * Math.PI * 164.81 * seconds) + Math.sin(2 * Math.PI * 207.65 * seconds) + Math.sin(2 * Math.PI * 246.94 * seconds));
        }
        final int samples_per_beat = (int) (SAMPLE_RATE * 60.0 / 128);
        final int kick_length = (int) (SAMPLE_RATE * 0.1);
        for (int kick_start = 0; kick_start + kick_length < sample_count; kick_start += samples_per_beat) {
            for (int offset = 0; offset < kick_length; offset++) {
                waveform[kick_start + offset] += 0.6 * Math.sin(2 * Math.PI * 60 * offset / SAMPLE_RATE) * Math.exp(-offset / (0.02 * SAMPLE_RATE));
            }
        }
        final Path track_wav = temp_dir.resolve("track.wav");
        Files.write(track_wav, encode_wav(waveform));

        final var result = MoodDetector.analyze(track_wav.toString());
        assertThat(result.tempo()).isBetween(125.0, 132.0);
        assertThat(result.key()).matches("[A-G]#? (major|minor)");
        assertThat(result.energy()).isBetween(0.0, 1.0);
    }

    private static boolean ffmpeg_available() throws InterruptedException {
        try {
            return new ProcessBuilder("ffmpeg", "-version").redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start().waitFor() == 0
                && new ProcessBuilder("ffprobe", "-version").redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start().waitFor() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    private static byte[] encode_wav(double[] samples) {
        final int data_byte_count = samples.length * 2;
        final ByteBuffer wav_bytes = ByteBuffer.allocate(44 + data_byte_count).order(ByteOrder.LITTLE_ENDIAN);
        wav_bytes.put("RIFF".getBytes()).putInt(36 + data_byte_count).put("WAVE".getBytes());
        wav_bytes.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 1).putInt(SAMPLE_RATE).putInt(SAMPLE_RATE * 2).putShort((short) 2).putShort((short) 16);
        wav_bytes.put("data".getBytes()).putInt(data_byte_count);
        for (double sample : samples) wav_bytes.putShort((short) Math.round(Math.max(-1, Math.min(1, sample)) * 32767));
        return wav_bytes.array();
    }
}
