package com.naugroup3.interlude.services.MusicAnalyzer.vibesnet;

import java.util.Arrays;

// based off of https://github.com/Subtitle-Synchronizer/jlibrosa/tree/master
final class MelSpectrogram {
    private static final int SR = AudioDecoder.SAMPLE_RATE;
    private static final int N_FFT = 1024;
    private static final int HOP = 320;
    private static final int WIN = 640;
    private static final int N_MELS = 128;
    private static final double F_MIN = 0.0;
    private static final double F_MAX = 8000.0;
    private static final double TOP_DB = 80.0;
    private static final double AMIN = 1e-10;

    private static final double[] WINDOW = hann(WIN);
    private static final float[][] FILTERS = mel_filters();

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
            fft(re, im);
            for (int k = 0; k < bins; k++) {
                power[t][k] = re[k] * re[k] + im[k] * im[k];
            }
        }

        float[][] db = new float[N_MELS][frames];
        double max = Double.NEGATIVE_INFINITY;
        double[][] raw = new double[N_MELS][frames];
        for (int m = 0; m < N_MELS; m++) {
            float[] f = FILTERS[m];
            for (int t = 0; t < frames; t++) {
                double s = 0;
                for (int k = 0; k < bins; k++) {
                    s += f[k] * power[t][k];
                }
                raw[m][t] = 10.0 * Math.log10(Math.max(AMIN, s));
                max = Math.max(max, raw[m][t]);
            }
        }
        double floor = max - TOP_DB;
        for (int m = 0; m < N_MELS; m++) {
            for (int t = 0; t < frames; t++) {
                db[m][t] = (float) Math.max(raw[m][t], floor);
            }
        }
        return db;
    }

    private static double[] hann(int n) {
        double[] w = new double[n];
        for (int i = 0; i < n; i++) {
            w[i] = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / n);
        }
        return w;
    }

    private static double hz_to_mel(double hz) {
        double fSp = 200.0 / 3, minLogHz = 1000.0, logStep = Math.log(6.4) / 27.0;
        double minLogMel = minLogHz / fSp;
        return hz >= minLogHz ? minLogMel + Math.log(hz / minLogHz) / logStep : hz / fSp;
    }

    private static double mel_to_hz(double mel) {
        double fSp = 200.0 / 3, minLogHz = 1000.0, logStep = Math.log(6.4) / 27.0;
        double minLogMel = minLogHz / fSp;
        return mel >= minLogMel ? minLogHz * Math.exp(logStep * (mel - minLogMel)) : mel * fSp;
    }

    private static float[][] mel_filters() {
        int bins = N_FFT / 2 + 1;
        double[] fft = new double[bins];
        for (int k = 0; k < bins; k++) {
            fft[k] = (double) k * SR / N_FFT;
        }
        double melMin = hz_to_mel(F_MIN), melMax = hz_to_mel(F_MAX);
        double[] pts = new double[N_MELS + 2];
        for (int i = 0; i < pts.length; i++) {
            pts[i] = mel_to_hz(melMin + (melMax - melMin) * i / (N_MELS + 1));
        }
        float[][] w = new float[N_MELS][bins];
        for (int m = 0; m < N_MELS; m++) {
            double lo = pts[m], mid = pts[m + 1], hi = pts[m + 2];
            double enorm = 2.0 / (hi - lo);
            for (int k = 0; k < bins; k++) {
                double lower = (fft[k] - lo) / (mid - lo);
                double upper = (hi - fft[k]) / (hi - mid);
                w[m][k] = (float) (Math.max(0, Math.min(lower, upper)) * enorm);
            }
        }
        return w;
    }

    private static void fft(double[] re, double[] im) {
        int n = re.length;
        for (int i = 1, j = 0; i < n; i++) {
            int bit = n >> 1;
            for (; (j & bit) != 0; bit >>= 1) {
                j ^= bit;
            }
            j ^= bit;
            if (i < j) {
                double t = re[i]; re[i] = re[j]; re[j] = t;
                t = im[i]; im[i] = im[j]; im[j] = t;
            }
        }
        for (int len = 2; len <= n; len <<= 1) {
            double ang = -2.0 * Math.PI / len;
            double wr = Math.cos(ang), wi = Math.sin(ang);
            for (int i = 0; i < n; i += len) {
                double cr = 1, ci = 0;
                for (int k = 0; k < len / 2; k++) {
                    int a = i + k, b = i + k + len / 2;
                    double xr = re[b] * cr - im[b] * ci;
                    double xi = re[b] * ci + im[b] * cr;
                    re[b] = re[a] - xr; im[b] = im[a] - xi;
                    re[a] += xr; im[a] += xi;
                    double ncr = cr * wr - ci * wi;
                    ci = cr * wi + ci * wr;
                    cr = ncr;
                }
            }
        }
    }
}
