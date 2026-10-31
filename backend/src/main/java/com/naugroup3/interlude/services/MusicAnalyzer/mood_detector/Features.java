package com.naugroup3.interlude.services.MusicAnalyzer.mood_detector;

import java.util.Arrays;

import com.naugroup3.interlude.services.MusicAnalyzer.AudioDecoder;
import com.naugroup3.interlude.services.MusicAnalyzer.AudioUtils;

final class Features {
    static final int SAMPLE_RATE = 22050;
    private static final int SR = SAMPLE_RATE;
    private static final int N_MELS = 128;
    private static final int N_CHROMA = 12;
    private static final double TOP_DB = 80.0;

    private static final double START_BPM = 120.0;
    private static final double MAX_TEMPO = 320.0;
    private static final int TEMPO_WIN = (int) (8.0 * SR / Stft.HOP);

    private static final double[][] MEL_FILTERS = AudioUtils.mel_filters(SR, Stft.N_FFT, N_MELS, 0.0, SR / 2.0);
    private static final double[][] CHROMA_FILTERS = chroma_filters(0.0);

    record Extracted(double tempo, double tempo_confidence, double energy, double spectral_centroid,
        double zero_crossing_rate, double[] chroma, double duration) {}

    private Features() {}

    static Extracted extract(AudioDecoder.Clip clip) {
        final float[] y = clip.samples();
        final double[][] power = Stft.power(y);

        final double[] onset_env = onset_strength(power);
        final double variance = variance(onset_env);
        final double tempo = variance < 0.01 ? 60.0 : estimate_tempo(onset_env);

        return new Extracted(tempo, variance, energy(y), spectral_centroid(power), zero_crossing_rate(y),
            chroma(power), clip.total_duration());
    }

    private static double energy(float[] y) {
        final int frame = Stft.N_FFT;
        final int pad = frame / 2;
        final double[] padded = new double[y.length + 2 * pad];
        for (int i = 0; i < y.length; i++) padded[pad + i] = y[i];
        final int frames = 1 + y.length / Stft.HOP;
        double sum = 0;
        for (int t = 0; t < frames; t++) {
            double sq = 0;
            for (int i = 0; i < frame; i++) {
                final double v = padded[t * Stft.HOP + i];
                sq += v * v;
            }
            sum += Math.sqrt(sq / frame);
        }
        return Math.min(Math.max(sum / frames * 2.5, 0.0), 1.0);
    }

    private static double spectral_centroid(double[][] power) {
        double sum = 0;
        for (double[] frame : power) {
            double total = 0, weighted = 0;
            for (int k = 0; k < Stft.BINS; k++) {
                final double mag = Math.sqrt(frame[k]);
                total += mag;
                weighted += mag * Stft.bin_frequency(k, SR);
            }
            sum += total > 0 ? weighted / total : 0.0;
        }
        return sum / power.length;
    }

    private static double zero_crossing_rate(float[] y) {
        final int frame = Stft.N_FFT;
        final int pad = frame / 2;
        final int n = y.length;
        final boolean[] negative = new boolean[n + 2 * pad];
        for (int i = 0; i < negative.length; i++) {
            negative[i] = y[Math.min(Math.max(i - pad, 0), n - 1)] < 0;
        }
        final int frames = 1 + n / Stft.HOP;
        double sum = 0;
        for (int t = 0; t < frames; t++) {
            int crossings = 0;
            final int start = t * Stft.HOP;
            for (int i = 1; i < frame; i++) {
                if (negative[start + i] != negative[start + i - 1]) crossings++;
            }
            sum += (double) crossings / (frame - 1);
        }
        return sum / frames;
    }

    private static double[] onset_strength(double[][] power) {
        final int frames = power.length;
        final double[][] db = AudioUtils.mel_db(power, MEL_FILTERS, TOP_DB);

        final int shift = 1 + Stft.N_FFT / (2 * Stft.HOP);
        final double[] env = new double[frames];
        for (int t = 1; t < frames; t++) {
            final int i = t - 1 + shift;
            if (i >= frames) break;
            double sum = 0;
            for (int m = 0; m < N_MELS; m++) {
                sum += Math.max(0.0, db[t][m] - db[t - 1][m]);
            }
            env[i] = sum / N_MELS;
        }
        return env;
    }

    private static double variance(double[] x) {
        double mean = 0;
        for (double v : x) mean += v;
        mean /= x.length;
        double var = 0;
        for (double v : x) var += (v - mean) * (v - mean);
        return var / x.length;
    }

    private static double estimate_tempo(double[] env) {
        final int n = env.length;
        final int win = TEMPO_WIN;
        final int half = win / 2;

        final double[] padded = new double[n + 2 * half];
        System.arraycopy(env, 0, padded, half, n);
        for (int j = 0; j < half; j++) {
            padded[j] = env[0] * j / half;
            padded[half + n + j] = env[n - 1] * (half - 1 - j) / half;
        }

        final double[] window = new double[win];
        for (int i = 0; i < win; i++) window[i] = 0.5 - 0.5 * Math.cos(2.0 * Math.PI * i / win);

        final double[] mean_ac = new double[win];
        final double[] frame = new double[win];
        final double[] ac = new double[win];
        for (int t = 0; t < n; t++) {
            for (int i = 0; i < win; i++) frame[i] = padded[t + i] * window[i];
            double peak = 0;
            for (int lag = 0; lag < win; lag++) {
                double s = 0;
                for (int i = 0; i + lag < win; i++) s += frame[i] * frame[i + lag];
                ac[lag] = s;
                peak = Math.max(peak, Math.abs(s));
            }
            final double scale = peak < Double.MIN_NORMAL ? 1.0 : peak;
            for (int lag = 0; lag < win; lag++) mean_ac[lag] += ac[lag] / scale;
        }
        for (int lag = 0; lag < win; lag++) mean_ac[lag] /= n;

        double best = Double.NEGATIVE_INFINITY;
        double tempo = 0.0;
        for (int lag = 1; lag < win; lag++) {
            final double bpm = 60.0 * SR / (Stft.HOP * lag);
            if (bpm >= MAX_TEMPO) continue;
            final double z = log2(bpm) - log2(START_BPM);
            final double score = Math.log1p(1e6 * mean_ac[lag]) - 0.5 * z * z;
            if (score > best) {
                best = score;
                tempo = bpm;
            }
        }
        return tempo;
    }

    private static double[] chroma(double[][] power) {
        final double tuning = estimate_tuning(power);
        final double[][] fb = tuning == 0.0 ? CHROMA_FILTERS : chroma_filters(tuning);

        final double[] mean = new double[N_CHROMA];
        final double[] raw = new double[N_CHROMA];
        for (double[] frame : power) {
            double max = 0;
            for (int c = 0; c < N_CHROMA; c++) {
                double s = 0;
                for (int k = 0; k < Stft.BINS; k++) s += fb[c][k] * frame[k];
                raw[c] = s;
                max = Math.max(max, Math.abs(s));
            }
            final double scale = max < Double.MIN_NORMAL ? 1.0 : max;
            for (int c = 0; c < N_CHROMA; c++) mean[c] += raw[c] / scale;
        }
        for (int c = 0; c < N_CHROMA; c++) mean[c] /= power.length;
        return mean;
    }

    private static double estimate_tuning(double[][] power) {
        final int frames = power.length;
        final double fmin = 150.0, fmax = Math.min(4000.0, SR / 2.0);

        final double[] pitches = new double[frames * Stft.BINS];
        final double[] mags = new double[frames * Stft.BINS];
        int count = 0;
        for (int t = 0; t < frames; t++) {
            final double[] s = power[t];
            double ref = 0;
            for (double v : s) ref = Math.max(ref, v);
            final double cutoff = 0.1 * ref;

            for (int k = 1; k < Stft.BINS - 1; k++) {
                final double f = Stft.bin_frequency(k, SR);
                if (f < fmin || f >= fmax || !(s[k] > cutoff)) continue;
                final double left = s[k - 1] > cutoff ? s[k - 1] : 0;
                final double right = s[k + 1] > cutoff ? s[k + 1] : 0;
                if (!(s[k] > left && s[k] >= right)) continue;

                final double avg = (s[k + 1] - s[k - 1]) / 2;
                double denom = 2 * s[k] - s[k + 1] - s[k - 1];
                if (Math.abs(denom) < Double.MIN_NORMAL) denom += 1;
                final double shift = avg / denom;
                pitches[count] = (k + shift) * SR / Stft.N_FFT;
                mags[count] = s[k] + 0.5 * avg * shift;
                count++;
            }
        }
        if (count == 0) return 0.0;

        final double[] sorted = Arrays.copyOf(mags, count);
        Arrays.sort(sorted);
        final double threshold = count % 2 == 1 ? sorted[count / 2] : (sorted[count / 2 - 1] + sorted[count / 2]) / 2;

        final int bins = 100;
        final int[] hist = new int[bins];
        for (int i = 0; i < count; i++) {
            if (mags[i] < threshold || pitches[i] <= 0) continue;
            double residual = (N_CHROMA * hz_to_octs(pitches[i], 0.0)) % 1.0;
            if (residual >= 0.5) residual -= 1.0;
            hist[Math.min((int) Math.floor((residual + 0.5) * bins), bins - 1)]++;
        }
        int best = 0;
        for (int i = 1; i < bins; i++) if (hist[i] > hist[best]) best = i;
        return -0.5 + best / (double) bins;
    }

    private static double log2(double x) {
        return Math.log(x) / Math.log(2);
    }

    private static double hz_to_octs(double hz, double tuning) {
        final double a440 = 440.0 * Math.pow(2.0, tuning / N_CHROMA);
        return log2(hz / (a440 / 16.0));
    }

    private static double[][] chroma_filters(double tuning) {
        final int n_fft = Stft.N_FFT;
        final double[] frq = new double[n_fft];
        for (int k = 1; k < n_fft; k++) {
            frq[k] = N_CHROMA * hz_to_octs((double) k * SR / n_fft, tuning);
        }
        frq[0] = frq[1] - 1.5 * N_CHROMA;

        final double[] width = new double[n_fft];
        for (int k = 0; k < n_fft - 1; k++) width[k] = Math.max(frq[k + 1] - frq[k], 1.0);
        width[n_fft - 1] = 1.0;

        final double[][] wts = new double[N_CHROMA][n_fft];
        final double half = Math.round(N_CHROMA / 2.0);
        for (int k = 0; k < n_fft; k++) {
            double norm = 0;
            for (int c = 0; c < N_CHROMA; c++) {
                final double x = frq[k] - c + half + 10 * N_CHROMA;
                final double d = x - N_CHROMA * Math.floor(x / N_CHROMA) - half;
                final double z = 2 * d / width[k];
                wts[c][k] = Math.exp(-0.5 * z * z);
                norm += wts[c][k] * wts[c][k];
            }
            norm = Math.sqrt(norm);
            final double o = (frq[k] / N_CHROMA - 5.0) / 2.0;
            final double octave = Math.exp(-0.5 * o * o);
            for (int c = 0; c < N_CHROMA; c++) {
                if (norm >= Double.MIN_NORMAL) wts[c][k] /= norm;
                wts[c][k] *= octave;
            }
        }

        final double[][] out = new double[N_CHROMA][Stft.BINS];
        for (int c = 0; c < N_CHROMA; c++) {
            System.arraycopy(wts[(c + 3) % N_CHROMA], 0, out[c], 0, Stft.BINS);
        }
        return out;
    }
}
