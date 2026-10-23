package com.naugroup3.interlude.services.MusicAnalyzer.vibesnet;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

final class AudioDecoder {
    static final int SAMPLE_RATE = 16000;

    private AudioDecoder() {}

    static float[] decode(String file_path) {
        Process proc = null;
        try {
            proc = new ProcessBuilder("ffmpeg", "-v", "error", "-i", file_path, "-f", "f32le", "-acodec", "pcm_f32le", "-ac", "1", "-ar", String.valueOf(SAMPLE_RATE), "-")
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
            byte[] raw;
            try (InputStream out = proc.getInputStream()) {
                raw = out.readAllBytes();
            }
            if (proc.waitFor() != 0 || raw.length == 0) {
                throw new IllegalArgumentException("Could not decode audio: " + file_path);
            }
            float[] samples = new float[raw.length / Float.BYTES];
            ByteBuffer.wrap(raw).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer().get(samples);
            return samples;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to run ffmpeg", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while decoding audio", e);
        } finally {
            if (proc != null) proc.destroy();
        }
    }
}
