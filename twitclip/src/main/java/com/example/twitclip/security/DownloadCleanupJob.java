package com.example.twitclip.security;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
public class DownloadCleanupJob {

    private static final File DOWNLOAD_DIR = new File("download");

    @Value("${app.cleanup.max-age-hours}")
    private long maxAgeHours;

    @PostConstruct
    public void cleanupOnStartup() {
        cleanupOldFiles();
    }

    @Scheduled(fixedDelay = 30 * 60 * 1000)
    public void cleanupOldFiles() {

        if (!DOWNLOAD_DIR.exists()) return;

        long now = Instant.now().toEpochMilli();
        long maxAgeMs = TimeUnit.HOURS.toMillis(maxAgeHours);

        File[] files = DOWNLOAD_DIR.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (now - file.lastModified() > maxAgeMs) {
                if (file.delete()) {
                    System.out.println("🧹 Deleted expired clip: " + file.getName());
                }
            }
        }
    }
}
