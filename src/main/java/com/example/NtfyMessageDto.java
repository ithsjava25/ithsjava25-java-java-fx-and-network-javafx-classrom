package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(
        String id,
        long time,
        String event,
        String topic,
        String message,
        Attachment attachment,     // används av ntfy vid multipart upload
        String imageUrl            // används vid egen JSON med bild-URL
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static record Attachment(
            String url,
            String name,
            String type,
            long size
    ) {}
}
