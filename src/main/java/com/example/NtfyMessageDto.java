package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

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
    public boolean hasAttachment() {
        return attachment != null;
    }

    public String getAttachmentUrl() {
        return attachment != null ? attachment.url() : null;
    }

    public String getAttachmentName() {
        return attachment != null ? attachment.name() : null;
    }

    public String getAttachmentContentType() {
        return attachment != null ? attachment.type() : null;
    }

    public String getFormattedTime() {
        return Instant.ofEpochSecond(time)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
record Attachment(String name, String url, String type, long size) {}
