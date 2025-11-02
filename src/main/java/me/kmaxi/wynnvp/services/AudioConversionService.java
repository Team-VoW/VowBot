package me.kmaxi.wynnvp.services;

import lombok.extern.slf4j.Slf4j;
import net.bramp.ffmpeg.FFmpeg;
import net.bramp.ffmpeg.FFmpegExecutor;
import net.bramp.ffmpeg.FFprobe;
import net.bramp.ffmpeg.builder.FFmpegBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * Service for converting audio files to MP3 format with compression
 * to reduce file size for Discord uploads.
 */
@Service
@Slf4j
public class AudioConversionService {

    private final FFmpeg ffmpeg;
    private final FFprobe ffprobe;

    public AudioConversionService() throws IOException {
        // FFmpeg should be available in system PATH
        this.ffmpeg = new FFmpeg("ffmpeg");
        this.ffprobe = new FFprobe("ffprobe");
        log.info("AudioConversionService initialized with FFmpeg");
    }

    /**
     * Converts an audio file to MP3 format with optimized settings for Discord.
     * Uses 128kbps bitrate and 44.1kHz sample rate to reduce file size while
     * maintaining acceptable quality for voice auditions.
     *
     * @param inputFile the audio file to convert
     * @return the converted MP3 file
     * @throws IOException if conversion fails
     */
    public File convertToMp3(File inputFile) throws IOException {
        // Check if already MP3 and small enough (under 8MB for Discord)
        String fileName = inputFile.getName().toLowerCase();
        long fileSizeInMB = inputFile.length() / (1024 * 1024);

        if (fileName.endsWith(".mp3") && fileSizeInMB < 7) {
            log.info("File {} is already MP3 and under 7MB, skipping conversion", inputFile.getName());
            return inputFile;
        }

        // Create output file with .mp3 extension
        String outputFileName = inputFile.getName().replaceAll("\\.[^.]+$", "") + "_converted.mp3";
        File outputFile = new File(inputFile.getParent(), outputFileName);

        log.info("Converting {} to MP3 format...", inputFile.getName());

        try {
            FFmpegBuilder builder = new FFmpegBuilder()
                    .setInput(inputFile.getAbsolutePath())
                    .overrideOutputFiles(true)
                    .addOutput(outputFile.getAbsolutePath())
                    .setAudioCodec("libmp3lame")
                    .setAudioBitRate(128_000)  // 128 kbps - good balance between quality and size
                    .setAudioChannels(1)       // Mono for voice recordings
                    .setAudioSampleRate(44_100) // 44.1 kHz sample rate
                    .done();

            FFmpegExecutor executor = new FFmpegExecutor(ffmpeg, ffprobe);
            executor.createJob(builder).run();

            log.info("Successfully converted {} to {}", inputFile.getName(), outputFile.getName());

            // Delete original file if conversion was successful and it's not the same file
            if (!inputFile.equals(outputFile)) {
                try {
                    Files.delete(inputFile.toPath());
                    log.debug("Deleted original file: {}", inputFile.getName());
                } catch (IOException e) {
                    log.warn("Failed to delete original file: {}", inputFile.getName(), e);
                }
            }

            return outputFile;

        } catch (Exception e) {
            // Clean up output file if it was created
            if (outputFile.exists()) {
                try {
                    Files.delete(outputFile.toPath());
                } catch (IOException ex) {
                    log.warn("Failed to delete failed conversion output: {}", outputFile.getName(), ex);
                }
            }
            throw new IOException("Failed to convert audio file: " + inputFile.getName(), e);
        }
    }
}

