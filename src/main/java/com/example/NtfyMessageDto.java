package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(
        String id,
        long time,
        String event,
        String topic,
        String message,
        AttachmentDto attachment
) {
    public String attachmentUrl() {
        return attachment != null ? attachment.url() : null;
    }
}
