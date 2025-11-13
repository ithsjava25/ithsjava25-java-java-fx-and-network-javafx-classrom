package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

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
    ) {
    }

    // DateTimeFormatter for displaying time
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

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

    // New method to get formatted time
    public String getFormattedTime() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(time),
                ZoneId.systemDefault()
        );
        return dateTime.format(TIME_FORMATTER);
    }

    // New method to get full formatted date and time
    public String getFormattedDateTime() {
        LocalDateTime dateTime = LocalDateTime.ofInstant(
                Instant.ofEpochSecond(time),
                ZoneId.systemDefault()
        );
        return dateTime.format(DATE_TIME_FORMATTER);
    }

    // New method to get display text for regular messages
    public String getDisplayText() {
        String timeStr = getFormattedTime();
        if (hasAttachment()) {
            return String.format("[%s] 📎 %s: %s", timeStr, topic, getAttachmentName());
        } else {
            String msg = message != null ? message : "";
            return String.format("[%s] %s: %s", timeStr, topic, msg);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NtfyMessageDto that = (NtfyMessageDto) o;
        return time == that.time && Objects.equals(id, that.id) && Objects.equals(event, that.event) && Objects.equals(topic, that.topic) && Objects.equals(title, that.title) && Objects.equals(message, that.message) && Objects.equals(attachment, that.attachment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time, event, topic, message, title, attachment);
    }

    @Override
    public String toString() {
        return "NtfyMessageDto {\n" +
                "  id='" + id + "'\n" +
                "  time=" + getFormattedDateTime() + " (" + time + ")\n" +
                "  event='" + event + "'\n" +
                "  topic='" + topic + "'\n" +
                "  message='" + message + "'\n" +
                "  title='" + title + "'\n" +
                "  attachment=" + (attachment != null ?
                "Attachment{name='" + attachment.name() +
                        "', url='" + attachment.url() +
                        "', contentType='" + attachment.contentType() +
                        "', size=" + attachment.size() +
                        ", expires=" + attachment.expires() + "}"
                : "null") + "\n" +
                "}";
    }
}