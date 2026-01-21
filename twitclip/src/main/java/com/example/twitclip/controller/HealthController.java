package com.example.twitclip.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
public class HealthController {

    @GetMapping("/")
    public Map<String, Object> health() {
        return Map.of(
                "status", "Video Clipper Server is running",
                "endpoints", "POST /clip, GET /download/{file}",
                "timestamp", Instant.now().toString()
        );
    }
}
