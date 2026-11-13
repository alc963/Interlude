package com.naugroup3.interlude.services.MusicAnalyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

class AudioUtilsTest {
    @Test
    void hann_window_is_periodic() {
        final double[] window = AudioUtils.periodic_hann_window(8);

        assertThat(window[0]).isZero();
        assertThat(window[4]).isCloseTo(1.0, within(1e-12));
        // periodic rather than symmetric, so the last sample mirrors the second
        assertThat(window[7]).isCloseTo(window[1], within(1e-12));
    }

    @Test
    void fft_maps_an_impulse_to_a_flat_spectrum_and_a_sine_to_its_bin() {
        final double[] impulse = new double[16], zeros = new double[16];
        impulse[0] = 1;
        AudioUtils.fft(impulse, zeros);
        assertThat(impulse).containsExactly(filled(16, 1.0), within(1e-12));
        assertThat(zeros).containsExactly(filled(16, 0.0), within(1e-12));

        final int n = 64, bin = 5;
        final double[] sine = new double[n], imaginary = new double[n];
        Arrays.setAll(sine, i -> Math.sin(2 * Math.PI * bin * i / n));
        AudioUtils.fft(sine, imaginary);
        for (int k = 0; k <= n / 2; k++) {
            assertThat(Math.hypot(sine[k], imaginary[k])).isCloseTo(k == bin ? n / 2.0 : 0.0, within(1e-9));
        }
    }

    @Test
    void mel_filters_are_non_negative_with_one_per_band() {
        final double[][] filters = AudioUtils.mel_filters(16000, 1024, 128, 0.0, 8000.0);

        assertThat(filters).hasDimensions(128, 513);
        for (double[] filter : filters) {
            assertThat(Arrays.stream(filter).min().getAsDouble()).isNotNegative();
            assertThat(Arrays.stream(filter).sum()).isPositive();
        }
    }

    @Test
    void mel_db_is_floored_top_db_below_its_peak() {
        final double[][] filters = AudioUtils.mel_filters(16000, 8, 2, 0.0, 8000.0);
        final double[] loud = { 1e8, 1e8, 1e8, 1e8, 1e8 }, quiet = { 1e-12, 1e-12, 1e-12, 1e-12, 1e-12 };

        final double[][] db = AudioUtils.mel_db(new double[][] { loud, quiet }, filters, 80.0);
        final double[] all = Arrays.stream(db).flatMapToDouble(Arrays::stream).toArray();
        assertThat(Arrays.stream(all).max().getAsDouble() - Arrays.stream(all).min().getAsDouble()).isCloseTo(80.0, within(1e-9));
    }

    private static double[] filled(int length, double value) {
        final double[] values = new double[length];
        Arrays.fill(values, value);
        return values;
    }
}
