package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Data Transfer Object for Ntfy messages
 *
 * @param id Unique message identifier
 * @param time Unix timestamp in seconds
 * @param event Type of event (message, open, keepalive)
 * @param topic Topic the message was sent to
 * @param message The message content
 * @param title Message title (optional)
 * @param attachment File attachment (optional)
 */
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

    public boolean hasAttachment() {return attachment != null;}
    public String getAttachmentUrl() {return attachment != null ? attachment.url() : null;}
    public String getAttachmentName() {return attachment != null ? attachment.name() : null;}
    public String getAttachmentContentType() {return attachment != null ? attachment.type() : null;}

    /**
     * Formats the timestamp to readable time
     * @return Formatted time string (HH:mm:ss)
     */
    public String getFormattedTime() {
        try { return Instant.ofEpochSecond(time).atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        } catch (Exception e) {return "Invalid time";}}

    @Override
    public String toString() {
        return "NtfyMessageDto{" +
                "id='" + id + '\'' +
                ", time=" + time +
                ", event='" + event + '\'' +
                ", topic='" + topic + '\'' +
                ", message='" + message + '\'' +
                ", title='" + title + '\'' +
                ", attachment=" + attachment +
                '}';
    }
}

/**
 * Represents a file attachment
 *
 * @param name File name
 * @param url Download URL
 * @param type Content type
 * @param size File size in bytes
 */
@JsonIgnoreProperties(ignoreUnknown = true)
record Attachment(String name, String url, String type, long size) {}