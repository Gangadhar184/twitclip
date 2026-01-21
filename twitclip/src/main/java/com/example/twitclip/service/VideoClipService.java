package com.example.twitclip.service;

import com.example.twitclip.dto.ClipRequest;
import com.example.twitclip.dto.ClipResponse;

public interface VideoClipService {
    ClipResponse clipVideo(ClipRequest request);
}
