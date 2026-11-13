package com.naugroup3.interlude.services.MusicAnalyzer.vibesnet;

import java.util.Arrays;

import com.naugroup3.interlude.services.MusicAnalyzer.AudioUtils;

// based off of https://github.com/Subtitle-Synchronizer/jlibrosa/tree/master
final class MelSpectrogram {
    static final int SAMPLE_RATE = 16000;
    private static final int SR = SAMPLE_RATE;
    private static final int N_FFT = 1024;
    private static final int HOP = 320;
    private static final int WIN = 640;
    private static final int N_MELS = 128;
    private static final double F_MIN = 0.0;
    private static final double F_MAX = 8000.0;
    private static final double TOP_DB = 80.0;

    private static final double[] WINDOW = AudioUtils.periodic_hann_window(WIN);
    private static final double[][] FILTERS = AudioUtils.mel_filters(SR, N_FFT, N_MELS, F_MIN, F_MAX);

    private MelSpectrogram() {}

    static float[][] extract(float[] y) {
        if (y.length < WIN) y = Arrays.copyOf(y, WIN);
        int frames = (y.length - WIN) / HOP + 1;
        int bins = N_FFT / 2 + 1;

        double[][] power = new double[frames][bins];
        double[] re = new double[N_FFT];
        double[] im = new double[N_FFT];
        for (int t = 0; t < frames; t++) {
            Arrays.fill(re, 0);
            Arrays.fill(im, 0);
            int start = t * HOP;
            for (int i = 0; i < WIN; i++) {
                re[i] = y[start + i] * WINDOW[i];
            }
            AudioUtils.fft(re, im);
            for (int k = 0; k < bins; k++) {
                power[t][k] = re[k] * re[k] + im[k] * im[k];
            }
        }

        double[][] mel = AudioUtils.mel_db(power, FILTERS, TOP_DB);
        float[][] db = new float[N_MELS][frames];
        for (int m = 0; m < N_MELS; m++) {
            for (int t = 0; t < frames; t++) {
                db[m][t] = (float) mel[t][m];
            }
        }
        return db;
    }
}
