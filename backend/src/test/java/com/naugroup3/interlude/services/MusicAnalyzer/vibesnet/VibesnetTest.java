package com.naugroup3.interlude.services.MusicAnalyzer.vibesnet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VibesnetTest {
    private static final int SAMPLE_RATE = MelSpectrogram.SAMPLE_RATE;

    @TempDir
    Path temp_dir;

    private static float[] sine(double hz, double seconds) {
        final float[] samples = new float[(int) (SAMPLE_RATE * seconds)];
        for (int index = 0; index < samples.length; index++) samples[index] = (float) (0.5 * Math.sin(2 * Math.PI * hz * index / SAMPLE_RATE));
        return samples;
    }

    private Path write_wav(float[] samples, int sample_rate) throws IOException {
        final int data_byte_count = samples.length * 2;
        final ByteBuffer wav_bytes = ByteBuffer.allocate(44 + data_byte_count).order(ByteOrder.LITTLE_ENDIAN);
        wav_bytes.put("RIFF".getBytes()).putInt(36 + data_byte_count).put("WAVE".getBytes());
        wav_bytes.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) 1).putInt(sample_rate).putInt(sample_rate * 2).putShort((short) 2).putShort((short) 16);
        wav_bytes.put("data".getBytes()).putInt(data_byte_count);
        for (float sample : samples) wav_bytes.putShort((short) Math.round(Math.max(-1, Math.min(1, sample)) * 32767));
        return Files.write(temp_dir.resolve("clip.wav"), wav_bytes.array());
    }

    private static boolean ffmpeg_available() throws InterruptedException {
        try {
            return new ProcessBuilder("ffmpeg", "-version").redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start().waitFor() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    @Test
    void mel_spectrogram_has_one_frame_per_hop_and_pads_short_audio() {
        final float[][] tone = MelSpectrogram.extract(sine(440, 3));
        assertThat(tone).hasNumberOfRows(128);
        assertThat(tone[0]).hasSize((3 * SAMPLE_RATE - 640) / 320 + 1);

        final float[][] short_clip = MelSpectrogram.extract(new float[100]);
        assertThat(short_clip).hasNumberOfRows(128);
        assertThat(short_clip[0]).hasSize(1);
    }

    @Test
    void mel_spectrogram_is_floored_80_db_below_peak_and_puts_a_tone_in_its_band() {
        final float[][] mel_spectrogram = MelSpectrogram.extract(sine(1000, 2));

        float loudest_db = Float.NEGATIVE_INFINITY, 
            quietest_db = Float.POSITIVE_INFINITY;
        int loudest_band = 0;
        for (int band = 0; band < mel_spectrogram.length; band++) {
            for (float db : mel_spectrogram[band]) {
                loudest_db = Math.max(loudest_db, db);
                quietest_db = Math.min(quietest_db, db);
            }
            if (mel_spectrogram[band][10] > mel_spectrogram[loudest_band][10]) loudest_band = band;
        }
        assertThat(loudest_db - quietest_db).isBetween(40.0f, 80.0001f);
        assertThat(loudest_band).isBetween(30, 50);
    }

    @Test
    void analyze_gives_deterministic_unit_range_scores_and_rejects_missing_files() throws IOException, InterruptedException {
        assumeTrue(ffmpeg_available(), "ffmpeg is not installed");
        final Path sine_wav = write_wav(sine(440, 5), SAMPLE_RATE);

        final Vibesnet.MusicAnalytics analytics = Vibesnet.analyze(sine_wav.toString());

        assertThat(List.of(
            analytics.acousticness(), analytics.danceability(), analytics.energy(), analytics.instrumentalness(),
            analytics.liveness(), analytics.speechiness(), analytics.valence()
        )).allSatisfy(score -> assertThat(score).isBetween(0.0, 1.0));
        assertThat(Vibesnet.analyze(sine_wav.toString())).isEqualTo(analytics);
        assertThatThrownBy(() -> Vibesnet.analyze(temp_dir.resolve("missing.wav").toString())).isInstanceOf(IllegalArgumentException.class);
    }
}
