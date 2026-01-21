package com.example.twitclip.controller;

import com.example.twitclip.dto.ClipRequest;
import com.example.twitclip.dto.ClipResponse;
import com.example.twitclip.service.VideoClipService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/clip")
public class VideoClipController {

    private final VideoClipService videoClipService;

    public VideoClipController(VideoClipService videoClipService) {
        this.videoClipService = videoClipService;
    }

    @PostMapping
    public ResponseEntity<ClipResponse> clipVideo(@RequestBody ClipRequest request) {
        if (request.tweetUrl() == null || request.tweetUrl().isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ClipResponse("tweetUrl is required"));
        }

        return ResponseEntity.ok(videoClipService.clipVideo(request));
    }
}
