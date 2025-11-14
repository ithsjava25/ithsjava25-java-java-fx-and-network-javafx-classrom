package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(
        String id,
        long time,
        String event,
        String topic,
        String message,
        @JsonProperty("file") String file,
        @JsonProperty("filename") String filename
) {

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NtfyMessageDto that = (NtfyMessageDto) o;
        return time == that.time &&
                Objects.equals(id, that.id) &&
                Objects.equals(event, that.event) &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, time, event, topic, message);
    }

    @Override
    public String toString() {
        return "NtfyMessageDto{" +
                "id='" + id + '\'' +
                ", time=" + time +
                ", event='" + event + '\'' +
                ", topic='" + topic + '\'' +
                ", message='" + message + '\'' +
                ", file='" + file + '\'' +
                ", filename='" + filename + '\'' +
                '}';
    }
}