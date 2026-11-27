package com.naugroup3.interlude.services.MusicAnalyzer.mood_detector;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

final class MoodClassifier {
    private static final String[] NOTES = { "C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B" };

    private record Detected(String mood, double tempo) {}
    private record Reference(double energy, double tempo, double brightness) {}

    private static final Map<String, Reference> REFERENCE_MOODS = Map.of(
        "House", new Reference(0.15, 125, 0.6),
        "Techno", new Reference(0.16, 130, 0.4),
        "Disco", new Reference(0.14, 115, 0.7),
        "Ambient", new Reference(0.04, 70, 0.5),
        "DnB", new Reference(0.18, 170, 0.5),
        "Downtempo", new Reference(0.08, 90, 0.4));
    private static final String[] REFERENCE_ORDER = { "House", "Techno", "Disco", "Ambient", "DnB", "Downtempo" };

    private MoodClassifier() {}

    static MoodDetector.MoodResult classify(Features.Extracted f) {
        final Detected detected = detect_mood(f.energy(), f.tempo(), f.tempo_confidence(), f.chroma(), f.spectral_centroid(), f.zero_crossing_rate());
        final String key = determine_key(f.chroma());
        return new MoodDetector.MoodResult(
            detected.mood(),
            f.energy(),
            Math.min(f.spectral_centroid() / 5000.0, 1.0),
            detected.tempo(),
            key,
            calculate_similarity_scores(f.energy(), detected.tempo(), f.spectral_centroid()),
            generate_explanation(f.energy(), detected.tempo(), f.spectral_centroid(), key));
    }

    private static Detected detect_mood(double energy, double tempo, double tempo_confidence, double[] chroma, double spectral_centroid, double zero_crossing_rate) {
        final double brightness = Math.min(spectral_centroid / 5000.0, 1.0);
        final boolean is_major = determine_major_minor(chroma);

        if (80 <= tempo && tempo <= 145 && energy > 0.8) {
            tempo = tempo * 2.0;
        }

        if (tempo_confidence < 0.2) {
            if (energy < 0.15) return new Detected("Ambient/Drone", tempo);
            if (energy < 0.30) return new Detected("Atmospheric/Textural", tempo);
            if (energy < 0.50) return new Detected("Atmospheric/Textural", tempo);
            if (energy < 0.70) return new Detected(brightness < 0.3 ? "Atmospheric/Textural" : "Noise/Experimental", tempo);
            return new Detected("Harsh Noise/Experimental", tempo);
        }

        if (zero_crossing_rate > 0.25) {
            return new Detected(energy > 0.3 ? "Harsh Noise/Experimental" : "Glitch/Experimental", tempo);
        }

        if (120 <= tempo && tempo <= 145 && energy >= 0.50) {
            return new Detected(brightness < 0.4 ? "Techno/Dark" : "Techno/Industrial", tempo);
        }

        if (100 <= tempo && tempo <= 130 && energy >= 0.15) {
            if (energy > 0.7) {
                if (115 <= tempo && tempo <= 130) {
                    return new Detected(brightness > 0.5 ? "Techno/Industrial" : "Techno/Dark", tempo);
                }
                return new Detected("Energetic/Rave", tempo);
            } else if (100 <= tempo && tempo <= 115 && energy < 0.5) {
                return new Detected("Disco/Funk", tempo);
            } else if (brightness > 0.5) {
                return new Detected(is_major ? "House/Dance" : "Dark House", tempo);
            } else if (brightness > 0.4) {
                return new Detected("Disco/Funk", tempo);
            }
            return new Detected("Club/Groovy", tempo);
        }

        if (148 <= tempo && tempo <= 180) {
            if (energy > 0.4) return new Detected("Drum & Bass", tempo);
            if (energy > 0.25) return new Detected("Breakbeat/Fast", tempo);
            return new Detected("Fast/Atmospheric", tempo);
        }

        if (tempo > 180 || energy > 0.7) {
            if (energy > 0.8) return new Detected("Hard/Aggressive", tempo);
            if (energy > 0.6) return new Detected("Energetic/Rave", tempo);
            return new Detected("Driving Electronic", tempo);
        }

        if (energy < 0.2) {
            if (tempo < 70) {
                if (energy < 0.1) return new Detected("Ambient/Atmospheric", tempo);
                if (!is_major) return new Detected("Melancholic/Sad", tempo);
                return new Detected("Downtempo/Relaxed", tempo);
            } else if (70 <= tempo && tempo < 100) {
                if (energy < 0.1 && brightness < 0.4) return new Detected("Downtempo/Dark", tempo);
                if (energy < 0.12) return new Detected("Ambient/Chill", tempo);
                return new Detected("Midtempo Groove", tempo);
            }
            return new Detected("Minimal/Sparse", tempo);
        }

        if (energy >= 0.25) {
            return new Detected(tempo > 100 ? "Upbeat/Moderate" : "Moderate Groove", tempo);
        } else if (energy >= 0.15) {
            return new Detected("Relaxed/Moderate", tempo);
        }
        return new Detected("Low Energy", tempo);
    }

    private static boolean determine_major_minor(double[] chroma) {
        final int root = argmax(chroma);
        return chroma[(root + 4) % 12] > chroma[(root + 3) % 12];
    }

    private static String determine_key(double[] chroma) {
        return NOTES[argmax(chroma)] + (determine_major_minor(chroma) ? " major" : " minor");
    }

    private static int argmax(double[] x) {
        int best = 0;
        for (int i = 1; i < x.length; i++) if (x[i] > x[best]) best = i;
        return best;
    }

    private static Map<String, Double> calculate_similarity_scores(double energy, double tempo, double spectral_centroid) {
        final double norm_brightness = Math.min(spectral_centroid / 5000.0, 1.0);
        final Map<String, Double> similarities = new LinkedHashMap<>();
        for (String mood : REFERENCE_ORDER) {
            final Reference ref = REFERENCE_MOODS.get(mood);
            final double energy_diff = Math.abs(energy - ref.energy());
            final double tempo_diff = Math.abs(tempo - ref.tempo()) / 100;
            final double brightness_diff = Math.abs(norm_brightness - ref.brightness());

            final double distance = Math.sqrt(Math.pow(energy_diff * 2, 2) + Math.pow(tempo_diff * 3, 2) + Math.pow(brightness_diff, 2));
            final double similarity = Math.max(0.0, 1.0 - distance / 3.0);
            similarities.put(mood, Math.rint(similarity * 100) / 100);
        }
        return similarities;
    }

    private static String generate_explanation(double energy, double tempo, double spectral_centroid, String key) {
        final double norm_brightness = Math.min(spectral_centroid / 5000.0, 1.0);

        final String energy_desc;
        if (energy < 0.1) energy_desc = "Very low";
        else if (energy < 0.2) energy_desc = "Low";
        else if (energy < 0.35) energy_desc = "Moderate";
        else if (energy < 0.5) energy_desc = "High";
        else energy_desc = "Very high";

        final String bright_desc;
        if (norm_brightness < 0.3) bright_desc = "dark/mellow";
        else if (norm_brightness < 0.6) bright_desc = "balanced";
        else bright_desc = "bright/sharp";

        final String tempo_desc;
        if (tempo < 80) tempo_desc = "slow";
        else if (tempo < 110) tempo_desc = "moderate";
        else if (tempo < 130) tempo_desc = "dance";
        else if (tempo < 150) tempo_desc = "fast";
        else tempo_desc = "very fast";

        return String.format(Locale.ROOT, "%s energy (%.3f), %s tempo (%.1f BPM), %s timbre, key of %s",
            energy_desc, energy, tempo_desc, tempo, bright_desc, key);
    }
}
