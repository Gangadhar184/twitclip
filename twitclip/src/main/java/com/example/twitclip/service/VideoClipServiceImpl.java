package com.example.twitclip.service;

import com.example.twitclip.dto.ClipRequest;
import com.example.twitclip.config.VirtualThreadConfig;
import com.example.twitclip.dto.ClipResponse;
import com.example.twitclip.security.SignedUrlService;
import com.example.twitclip.util.CommandExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class VideoClipServiceImpl implements VideoClipService {

    @Value("${app.base-url}")
    private String baseUrl;

    @Value("${app.download.url-expiry-minutes:10}")
    private long expiryMinutes;

    private final CommandExecutor executor;
    private final SignedUrlService signedUrlService;
    private final Executor taskExecutor;

    public VideoClipServiceImpl(CommandExecutor executor, SignedUrlService signedUrlService, Executor taskExecutor) {
        this.executor = executor;
        this.signedUrlService = signedUrlService;
        this.taskExecutor = taskExecutor;
    }

    @Override
    public ClipResponse clipVideo(ClipRequest request) {

       return  CompletableFuture
               .supplyAsync(() -> processClip(request), taskExecutor)
               .join();
    }

    private ClipResponse processClip(ClipRequest request) {
        validateRequest(request);
        ensureBinariesExist();

        String tweetUrl = request.tweetUrl().replace("x.com", "twitter.com");
        long timestamp = Instant.now().getEpochSecond();

        File downloadDir = new File("download");
        if (!downloadDir.exists() && !downloadDir.mkdirs()) {
            throw new IllegalStateException("Failed to create download directory");
        }

        String videoFile = "download/raw_" + timestamp + ".mp4";
        String clippedFile = "download/clipped_" + timestamp + ".mp4";

        executor.execute(
                "yt-dlp",
                "-f", "mp4",
                "-o", videoFile,
                tweetUrl
        );

        if (!new File(videoFile).exists()) {
            throw new IllegalStateException("Video download failed");
        }

        buildFfmpegCommand(request, videoFile, clippedFile);

        File outputFile = new File(clippedFile);
        if (!outputFile.exists()) {
            throw new IllegalStateException("Clip file not created");
        }

        long expiresAt = Instant.now()
                .plus(expiryMinutes, ChronoUnit.MINUTES)
                .toEpochMilli();

        String fileName = outputFile.getName();
        String signature = signedUrlService.generateSignature(fileName, expiresAt);

        String downloadUrl =
                baseUrl + "/download/" + fileName +
                        "?expires=" + expiresAt +
                        "&sig=" + signature;

        return new ClipResponse(downloadUrl);
    }


    private void buildFfmpegCommand(ClipRequest request, String input, String output) {

        String start = request.start();
        String end = request.end();

        if (start != null && end != null) {
            executor.execute(
                    "ffmpeg",
                    "-ss", start,
                    "-to", end,
                    "-i", input,
                    "-c", "copy",
                    output
            );
        } else if (start != null) {
            executor.execute(
                    "ffmpeg",
                    "-ss", start,
                    "-i", input,
                    "-c", "copy",
                    output
            );
        } else if (end != null) {
            executor.execute(
                    "ffmpeg",
                    "-i", input,
                    "-to", end,
                    "-c", "copy",
                    output
            );
        } else {
            executor.execute(
                    "ffmpeg",
                    "-i", input,
                    "-c", "copy",
                    output
            );
        }
    }

    private void validateRequest(ClipRequest request) {

        if (request.tweetUrl() == null ||
                !request.tweetUrl().matches("^https://(x|twitter)\\.com/.+/status/\\d+.*$")) {
            throw new IllegalArgumentException("Invalid Twitter/X URL");
        }

        if (request.start() != null && !request.start().matches("\\d{2}:\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("Start must be HH:mm:ss");
        }

        if (request.end() != null && !request.end().matches("\\d{2}:\\d{2}:\\d{2}")) {
            throw new IllegalArgumentException("End must be HH:mm:ss");
        }
    }

    private void ensureBinariesExist() {
        executor.execute("yt-dlp", "--version");
        executor.execute("ffmpeg", "-version");
    }
}
