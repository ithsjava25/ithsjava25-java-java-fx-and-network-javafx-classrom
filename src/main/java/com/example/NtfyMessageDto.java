package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(
        String id,
        long time,
        String event,
        String topic,
        String message,
        Attachment attachment // nytt fält
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attachment(String name, String type, long size, String url) { }
}
