package com.naugroup3.interlude.services.MusicAnalyzer;

// a lot based of librosa
public final class AudioUtils {
    private static final double MIN_POWER = 1e-10;

    private static final double MEL_HZ_PER_STEP_LINEAR = 200.0 / 3;
    private static final double MEL_LOG_REGION_START_HZ = 1000.0;
    private static final double MEL_LOG_STEP = Math.log(6.4) / 27.0;

    private AudioUtils() {}

    public static double[] periodic_hann_window(int window_length) {
        final double[] window = new double[window_length];
        for (int sample = 0; sample < window_length; sample++) {
            window[sample] = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * sample / window_length);
        }
        return window;
    }

    // length must be a power of two
    public static void fft(double[] real, double[] imaginary) {
        final int length = real.length;

        for (int index = 1, reversed = 0; index < length; index++) {
            int bit = length >> 1;
            for (; (reversed & bit) != 0; bit >>= 1) {
                reversed ^= bit;
            }
            reversed ^= bit;
            if (index < reversed) {
                double swap = real[index]; real[index] = real[reversed]; real[reversed] = swap;
                swap = imaginary[index]; imaginary[index] = imaginary[reversed]; imaginary[reversed] = swap;
            }
        }

        for (int size = 2; size <= length; size <<= 1) {
            final int half_size = size / 2;
            final double step_angle = -2.0 * Math.PI / size;
            final double step_real = Math.cos(step_angle), step_imaginary = Math.sin(step_angle);
            for (int block_start = 0; block_start < length; block_start += size) {
                double twiddle_real = 1, twiddle_imaginary = 0;
                for (int offset = 0; offset < half_size; offset++) {
                    final int top = block_start + offset, bottom = block_start + offset + half_size;
                    final double product_real = real[bottom] * twiddle_real - imaginary[bottom] * twiddle_imaginary;
                    final double product_imaginary = real[bottom] * twiddle_imaginary + imaginary[bottom] * twiddle_real;
                    real[bottom] = real[top] - product_real;
                    imaginary[bottom] = imaginary[top] - product_imaginary;
                    real[top] += product_real;
                    imaginary[top] += product_imaginary;
                    final double next_twiddle_real = twiddle_real * step_real - twiddle_imaginary * step_imaginary;
                    twiddle_imaginary = twiddle_real * step_imaginary + twiddle_imaginary * step_real;
                    twiddle_real = next_twiddle_real;
                }
            }
        }
    }

    public static double[][] mel_filters(int sample_rate, int n_fft, int n_mels, double min_hz, double max_hz) {
        final int fft_bins = n_fft / 2 + 1;
        final double min_mel = hz_to_mel(min_hz), max_mel = hz_to_mel(max_hz);

        final double[] edge_hz = new double[n_mels + 2];
        for (int edge = 0; edge < edge_hz.length; edge++) {
            edge_hz[edge] = mel_to_hz(min_mel + (max_mel - min_mel) * edge / (n_mels + 1));
        }

        final double[][] filters = new double[n_mels][fft_bins];
        for (int band = 0; band < n_mels; band++) {
            final double lower_hz = edge_hz[band], center_hz = edge_hz[band + 1], upper_hz = edge_hz[band + 2];
            final double area_normalization = 2.0 / (upper_hz - lower_hz);
            for (int bin = 0; bin < fft_bins; bin++) {
                final double bin_hz = (double) bin * sample_rate / n_fft;
                final double rising_slope = (bin_hz - lower_hz) / (center_hz - lower_hz);
                final double falling_slope = (upper_hz - bin_hz) / (upper_hz - center_hz);
                filters[band][bin] = Math.max(0, Math.min(rising_slope, falling_slope)) * area_normalization;
            }
        }
        return filters;
    }

    public static double[][] mel_db(double[][] power_spectrogram, double[][] mel_filters, double top_db) {
        final double[][] mel_db = new double[power_spectrogram.length][mel_filters.length];
        double peak_db = Double.NEGATIVE_INFINITY;
        for (int frame = 0; frame < power_spectrogram.length; frame++) {
            for (int band = 0; band < mel_filters.length; band++) {
                final double[] band_filter = mel_filters[band];
                double band_power = 0;
                for (int bin = 0; bin < band_filter.length; bin++) {
                    band_power += band_filter[bin] * power_spectrogram[frame][bin];
                }
                mel_db[frame][band] = 10.0 * Math.log10(Math.max(MIN_POWER, band_power));
                peak_db = Math.max(peak_db, mel_db[frame][band]);
            }
        }

        final double floor_db = peak_db - top_db;
        for (double[] frame_db : mel_db) {
            for (int band = 0; band < frame_db.length; band++) {
                frame_db[band] = Math.max(frame_db[band], floor_db);
            }
        }
        return mel_db;
    }

    private static double hz_to_mel(double hz) {
        final double log_region_start_mel = MEL_LOG_REGION_START_HZ / MEL_HZ_PER_STEP_LINEAR;
        return hz >= MEL_LOG_REGION_START_HZ
            ? log_region_start_mel + Math.log(hz / MEL_LOG_REGION_START_HZ) / MEL_LOG_STEP
            : hz / MEL_HZ_PER_STEP_LINEAR;
    }

    private static double mel_to_hz(double mel) {
        final double log_region_start_mel = MEL_LOG_REGION_START_HZ / MEL_HZ_PER_STEP_LINEAR;
        return mel >= log_region_start_mel
            ? MEL_LOG_REGION_START_HZ * Math.exp(MEL_LOG_STEP * (mel - log_region_start_mel))
            : mel * MEL_HZ_PER_STEP_LINEAR;
    }
}
