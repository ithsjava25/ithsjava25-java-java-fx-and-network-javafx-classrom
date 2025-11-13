package com.example.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessage(
        String id,
        Long time,
        String event,
        String topic,
        String message,
        String attachment  // Nytt fält för filer
) {
    /**
     * Creates a NtfyMessage with the specified topic and message, using default values for id, time, event, and attachment.
     *
     * @param topic   the topic for the message
     * @param message the message body
     */
    public NtfyMessage(String topic, String message) {
        this(null, null, "message", topic, message, null);
    }

    /**
     * Creates a NtfyMessage for the given topic containing the provided message and attachment,
     * with `id` and `time` set to null and `event` set to "message".
     *
     * @param topic      the message topic
     * @param message    the message payload
     * @param attachment optional attachment data (e.g., serialized file content or reference)
     */
    public NtfyMessage(String topic, String message, String attachment) {
        this(null, null, "message", topic, message, attachment);
    }
}