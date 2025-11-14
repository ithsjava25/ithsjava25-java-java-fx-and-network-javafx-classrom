package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Data transfer object for messages from the Ntfy server.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(String id, long time, String event, String topic, String message) {
}