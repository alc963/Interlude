package com.naugroup3.interlude.services.MusicAnalyzer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.offset;
import static org.assertj.core.api.Assertions.withinPercentage;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AudioDecoderTest {
    private static final int FILE_SAMPLE_RATE = 44100;

    @TempDir
    Path temp_dir;

    @BeforeEach
    void requires_ffmpeg() throws InterruptedException {
        assumeTrue(tool_available("ffmpeg") && tool_available("ffprobe"), "ffmpeg is not installed");
    }

    private static boolean tool_available(String tool) throws InterruptedException {
        try {
            return new ProcessBuilder(tool, "-version").redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.DISCARD).start().waitFor() == 0;
        } catch (IOException e) {
            return false;
        }
    }

    private Path write_wav(String file_name, double duration_seconds, double[] channel_gains) throws IOException {
        final int channel_count = channel_gains.length;
        final int frame_count = (int) (FILE_SAMPLE_RATE * duration_seconds);
        final int data_byte_count = frame_count * channel_count * 2;
        final ByteBuffer wav_bytes = ByteBuffer.allocate(44 + data_byte_count).order(ByteOrder.LITTLE_ENDIAN);
        wav_bytes.put("RIFF".getBytes()).putInt(36 + data_byte_count).put("WAVE".getBytes());
        wav_bytes.put("fmt ".getBytes()).putInt(16).putShort((short) 1).putShort((short) channel_count).putInt(FILE_SAMPLE_RATE)
            .putInt(FILE_SAMPLE_RATE * channel_count * 2).putShort((short) (channel_count * 2)).putShort((short) 16);
        wav_bytes.put("data".getBytes()).putInt(data_byte_count);
        for (int frame = 0; frame < frame_count; frame++) {
            final double sample = 0.5 * Math.sin(2 * Math.PI * 440 * frame / FILE_SAMPLE_RATE);
            for (double gain : channel_gains) wav_bytes.putShort((short) Math.round(sample * gain * 32767));
        }
        return Files.write(temp_dir.resolve(file_name), wav_bytes.array());
    }
    private static double root_mean_square(float[] samples) {
        double sum_of_squares = 0;
        for (float sample : samples) sum_of_squares += sample * sample;
        return Math.sqrt(sum_of_squares / samples.length);
    }

    @Test
    void decodes_whole_file_at_the_requested_rate() throws IOException {
        final Path one_second_wav = write_wav("one_second.wav", 1, new double[] { 1 });
        assertThat(AudioDecoder.decode(one_second_wav.toString(), 16000).length).isBetween(15900, 16100);
        assertThat(AudioDecoder.decode(one_second_wav.toString(), 22050).length).isBetween(22000, 22100);
    }

    @Test
    void averages_channels_instead_of_summing_power() throws IOException {
        final double mono_level = root_mean_square(AudioDecoder.decode(write_wav("mono.wav", 1, new double[] { 1 }).toString(), 22050));
        final double stereo_level = root_mean_square(AudioDecoder.decode(write_wav("stereo.wav", 1, new double[] { 1, 1 }).toString(), 22050));
        final double out_of_phase_level = root_mean_square(AudioDecoder.decode(write_wav("opposite.wav", 1, new double[] { 1, -1 }).toString(), 22050));

        assertThat(stereo_level).isCloseTo(mono_level, withinPercentage(1));
        assertThat(out_of_phase_level).isLessThan(mono_level * 0.01);
    }

    @Test
    void short_tracks_are_decoded_from_the_start() throws IOException {
        final var clip = AudioDecoder.decode_skip_intro(write_wav("short.wav", 10, new double[] { 1 }).toString(), 22050);

        assertThat(clip.total_duration()).isCloseTo(10.0, offset(0.05));
        assertThat(clip.samples().length).isBetween(10 * 22050 - 200, 10 * 22050);
    }

    @Test
    void medium_tracks_skip_the_first_quarter() throws IOException {
        final var clip = AudioDecoder.decode_skip_intro(write_wav("medium.wav", 40, new double[] { 1 }).toString(), 22050);

        assertThat(clip.total_duration()).isCloseTo(40.0, offset(0.05));
        assertThat(clip.samples().length).isBetween(30 * 22050 - 200, 30 * 22050);
    }

    @Test
    void long_tracks_decode_30_seconds_from_the_middle() throws IOException {
        final var clip = AudioDecoder.decode_skip_intro(write_wav("long.wav", 70, new double[] { 1 }).toString(), 22050);

        assertThat(clip.total_duration()).isCloseTo(70.0, offset(0.05));
        assertThat(clip.samples().length).isBetween(30 * 22050 - 200, 30 * 22050);
    }

    @Test
    void rejects_files_that_are_not_audio() throws IOException {
        final Path text_file = Files.writeString(temp_dir.resolve("notes.wav"), "definitely not audio");

        assertThatThrownBy(() -> AudioDecoder.decode(text_file.toString(), 16000)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AudioDecoder.decode_skip_intro(text_file.toString(), 22050)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_a_missing_file() {
        assertThatThrownBy(() -> AudioDecoder.decode(temp_dir.resolve("missing.wav").toString(), 16000)).isInstanceOf(IllegalArgumentException.class);
    }
}
