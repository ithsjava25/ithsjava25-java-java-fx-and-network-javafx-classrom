package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(
        String id,
        long time,
        String event,
        String topic,
        String message,
        String title,
        @JsonProperty("attachment") Attachment attachment
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attachment(
            String name,
            String url,
            @JsonProperty("content-type") String contentType,
            long size,
            long expires
    ) {}

    public boolean hasAttachment() {
        return attachment != null;
    }

    public String getAttachmentName() {
        return attachment != null ? attachment.name() : null;
    }

    public String getAttachmentUrl() {
        return attachment != null ? attachment.url() : null;
    }

    public String getAttachmentContentType() {
        return attachment != null ? attachment.contentType() : null;
    }
}