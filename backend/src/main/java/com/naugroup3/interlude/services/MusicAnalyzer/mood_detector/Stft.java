package com.naugroup3.interlude.services.MusicAnalyzer.mood_detector;

import java.util.Arrays;

import com.naugroup3.interlude.services.MusicAnalyzer.AudioUtils;

// librosa.stft
final class Stft {
    static final int N_FFT = 2048;
    static final int HOP = 512;
    static final int BINS = N_FFT / 2 + 1;

    private static final double[] WINDOW = AudioUtils.periodic_hann_window(N_FFT);

    private Stft() {}

    static double[][] power(float[] y) {
        final int pad = N_FFT / 2;
        final double[] padded = new double[y.length + 2 * pad];
        for (int i = 0; i < y.length; i++) padded[pad + i] = y[i];

        final int frames = 1 + y.length / HOP;
        final double[][] power = new double[frames][BINS];
        final double[] re = new double[N_FFT];
        final double[] im = new double[N_FFT];
        for (int t = 0; t < frames; t++) {
            Arrays.fill(im, 0);
            final int start = t * HOP;
            for (int i = 0; i < N_FFT; i++) {
                re[i] = padded[start + i] * WINDOW[i];
            }
            AudioUtils.fft(re, im);
            for (int k = 0; k < BINS; k++) {
                power[t][k] = re[k] * re[k] + im[k] * im[k];
            }
        }
        return power;
    }

    static double bin_frequency(int k, int sr) {
        return (double) k * sr / N_FFT;
    }
}
