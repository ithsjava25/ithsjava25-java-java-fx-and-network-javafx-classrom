package com.example.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessage(
        String id,
        Long time,
        String event,
        String topic,
        String message
) {
}
