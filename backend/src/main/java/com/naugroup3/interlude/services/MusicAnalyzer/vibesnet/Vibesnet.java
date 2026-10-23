package com.naugroup3.interlude.services.MusicAnalyzer.vibesnet;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.Map;

public final class Vibesnet {
    public record MusicAnalytics(double acousticness, double danceability, double energy, 
        double instrumentalness, double liveness, double speechiness, double valence) {}
    
    private static final String MODEL_PATH = "/vibesnet/efficientnet_model.onnx";

    private static OrtEnvironment env;
    private static OrtSession session;

    private Vibesnet() {}

    public static MusicAnalytics analyze(String file_path) {
        float[] waveform = AudioDecoder.decode(file_path);
        float[][] mel = MelSpectrogram.extract(waveform);
        float[] logits = infer(mel);
        return to_analytics(logits);
    }

    private static synchronized OrtSession session() throws OrtException, IOException {
        if (session == null) {
            try (InputStream input = Vibesnet.class.getResourceAsStream(MODEL_PATH)) {
                if (input == null) {
                    throw new IllegalStateException("Missing model resource " + MODEL_PATH);
                }
                env = OrtEnvironment.getEnvironment();
                session = env.createSession(input.readAllBytes(), new OrtSession.SessionOptions());
            }
        }
        return session;
    }

    private static float[] infer(float[][] mel) {
        final int n_mels = mel.length;
        final int frames = mel[0].length;
        final FloatBuffer buf = ByteBuffer.allocateDirect(n_mels * frames * Float.BYTES).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for(float[] row : mel) buf.put(row);
        buf.rewind();

        try {
            final OrtSession s = session();
            try (final OnnxTensor x = OnnxTensor.createTensor(env, buf, new long[] { 1, n_mels, frames });
                    final OrtSession.Result out = s.run(Map.of("x", x))) {
                return ((float[][]) out.get(0).getValue())[0];
            }
        } catch (OrtException | IOException e) {
            throw new IllegalStateException("vibesnet inference failed", e);
        }
    }

    // Model output: acousticness, danceability, energy, instrumentalness, liveness, speechiness, valence
    private static MusicAnalytics to_analytics(float[] l) {
        return new MusicAnalytics(
            sigmoid(l[0]),
            clip01(l[1]),
            clip01(l[2]),
            sigmoid(l[3]),
            sigmoid(l[4]),
            clip01(l[5]),
            clip01(l[6]));
    }

    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }
    private static double clip01(double x) {
        return Math.max(0.0, Math.min(1.0, x));
    }
}
