package com.naugroup3.interlude.services.MusicAnalyzer;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public final class AudioDecoder {
    public record Clip(float[] samples, double total_duration) {}
    private record Probe(double duration_sec, int channels) {}

    private AudioDecoder() {}

    public static float[] decode(String file_path, int sample_rate) {
        final Probe probe = probe(file_path);
        return decode(file_path, sample_rate, 0, probe.duration_sec(), probe.channels());
    }

    public static Clip decode_skip_intro(String file_path, int sample_rate) {
        final Probe probe = probe(file_path);
        final double total_duration_sec = probe.duration_sec();
        final double offset_duration_sec;
        final double window_size_sec;
        if(total_duration_sec > 60) {
            offset_duration_sec = 30.0;
            window_size_sec = 30.0;
        } else if (total_duration_sec > 30) {
            offset_duration_sec = total_duration_sec * 0.25;
            window_size_sec = Math.min(30.0, total_duration_sec - offset_duration_sec);
        } else {
            offset_duration_sec = 0;
            window_size_sec = Math.min(30.0, total_duration_sec);
        }
        return new Clip(decode(file_path, sample_rate, offset_duration_sec, window_size_sec, probe.channels()), total_duration_sec);
    }

    private static Probe probe(String file_path) {
        Process process = null;
        try {
            process = new ProcessBuilder("ffprobe", "-v", "error", "-select_streams", "a:0", "-show_entries", "format=duration:stream=channels", "-of", "default=noprint_wrappers=1", file_path)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
            final String output;
            try(InputStream in = process.getInputStream()) {
                output = new String(in.readAllBytes());
            }
            if(process.waitFor() != 0) {
                throw new IllegalArgumentException("Could not read audio info: " + file_path);
            }
            double duration_sec = -1;
            int channels = -1;
            for(String line : output.split("\\R")) {
                if(line.startsWith("duration=")) duration_sec = Double.parseDouble(line.substring("duration=".length()).trim());
                else if(line.startsWith("channels=")) channels = Integer.parseInt(line.substring("channels=".length()).trim());
            }
            if(duration_sec <= 0 || channels <= 0) {
                throw new IllegalArgumentException("Could not read audio info: " + file_path);
            }
            return new Probe(duration_sec, channels);
        } catch(NumberFormatException e) {
            throw new IllegalArgumentException("Could not read audio info: " + file_path, e);
        } catch(IOException e) {
            throw new IllegalStateException("Failed to run ffprobe", e);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while probing audio", e);
        } finally {
            if(process != null) process.destroy();
        }
    }

    private static float[] decode(String file_path, int sample_rate, double offset, double length, int channels) {
        Process process = null;
        try {
            process = new ProcessBuilder("ffmpeg", "-v", "error", "-ss", String.valueOf(offset), "-t", String.valueOf(length), "-i", file_path,
                    "-af", mono_mix(channels), "-f", "f32le", "-acodec", "pcm_f32le", "-ar", String.valueOf(sample_rate), "-")
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
            byte[] raw_bytes;
            try(InputStream out = process.getInputStream()) {
                raw_bytes = out.readAllBytes();
            }
            if(process.waitFor() != 0 || raw_bytes.length == 0) {
                throw new IllegalArgumentException("Could not decode audio: " + file_path);
            }
            float[] samples = new float[raw_bytes.length / Float.BYTES];
            ByteBuffer.wrap(raw_bytes).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(samples);
            // librosa.load never returns more than the requested window
            final int max = (int)Math.round(length * sample_rate);
            return samples.length > max ? Arrays.copyOf(samples, max) : samples;
        } catch(IOException e) {
            throw new IllegalStateException("Failed to run ffmpeg", e);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while decoding audio", e);
        } finally {
            if (process != null) process.destroy();
        }
    }

    private static String mono_mix(int channels) {
        final String weight = String.format(Locale.ROOT, "%.9f", 1.0 / channels);
        return "pan=mono|c0=" + IntStream
            .range(0, channels)
            .mapToObj(channel -> weight + "*c" + channel)
            .collect(Collectors.joining("+"));
    }
}
