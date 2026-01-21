package com.example.twitclip.service;

import com.example.twitclip.dto.ClipRequest;
import com.example.twitclip.dto.ClipResponse;
import com.example.twitclip.util.CommandExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.Instant;

@Service
public class VideoClipServiceImpl implements VideoClipService {

    @Value("${app.base-url}")
    private String baseUrl;

    private final CommandExecutor executor;

    public VideoClipServiceImpl(CommandExecutor executor) {
        this.executor = executor;
    }

    @Override
    public ClipResponse clipVideo(ClipRequest request) {

        String tweetUrl = request.tweetUrl()
                .replace("x.com", "twitter.com");

        long timestamp = Instant.now().getEpochSecond();

        File downloadDir = new File("download");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }

        String videoFile = "download/" + timestamp + ".mp4";
        String clippedFile = "download/clipped_" + timestamp + ".mp4";

        // Download video
        executor.execute(
                "yt-dlp",
                "-o", videoFile,
                tweetUrl
        );

        String start = request.start();
        String end = request.end();

        // Clip logic
        if (start == null && end == null) {
            executor.execute(
                    "ffmpeg",
                    "-i", videoFile,
                    "-c", "copy",
                    clippedFile
            );
        } else if (end == null) {
            executor.execute(
                    "ffmpeg",
                    "-i", videoFile,
                    "-ss", start,
                    "-c", "copy",
                    clippedFile
            );
        } else if (start == null) {
            executor.execute(
                    "ffmpeg",
                    "-i", videoFile,
                    "-to", end,
                    "-c", "copy",
                    clippedFile
            );
        } else {
            executor.execute(
                    "ffmpeg",
                    "-i", videoFile,
                    "-ss", start,
                    "-to", end,
                    "-c", "copy",
                    clippedFile
            );
        }

        return new ClipResponse(baseUrl + "/" + clippedFile);
    }
}
