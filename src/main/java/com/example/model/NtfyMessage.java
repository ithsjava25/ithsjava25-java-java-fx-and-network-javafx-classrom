package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessage(
        String id,
        Long time,
        String event,
        String topic,
        String message,
        String attachment
) {
    public NtfyMessage(String topic, String message) {
        this(null, null, "message", topic, message, null);
    }

    public NtfyMessage(String topic, String message, String attachment) {
        this(null, null, "message", topic, message, attachment);
    }
}