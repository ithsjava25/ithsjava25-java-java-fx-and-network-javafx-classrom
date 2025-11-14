package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;
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
    public boolean hasAttachment() { return attachment != null; }
    public String getAttachmentUrl() { return attachment != null ? attachment.url() : null; }
    public String getAttachmentName() { return attachment != null ? attachment.name() : null; }
    public String getAttachmentContentType() { return attachment != null ? attachment.type() : null; }

    public String getFormattedTime() {
        return Instant.ofEpochSecond(time)
                .atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern("HH:mm:ss"));
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
        DateTimeFormatter swedishFormatter = DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());
        String formattedTime = swedishFormatter.format(Instant.ofEpochSecond(time));

        return "NtfyMessageDto{" +
                "id='" + id + '\'' +
                ", time=" + formattedTime +
                ", event='" + event + '\'' +
                ", topic='" + topic + '\'' +
                ", message='" + message + '\'' +
                ", title='" + title + '\'' +
                ", attachment=" + attachment +
                '}';
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
record Attachment(String name, String url, String type, long size) {}
