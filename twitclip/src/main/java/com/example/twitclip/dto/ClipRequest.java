package com.example.twitclip.dto;


public record ClipRequest(
        String tweetUrl,
        String start,
        String end
) {}

