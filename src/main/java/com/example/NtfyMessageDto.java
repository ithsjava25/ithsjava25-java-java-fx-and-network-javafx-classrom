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
        String[] tags,
        @JsonProperty("attachment") Attachment attachment
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Attachment(
            String name,
            String url,
            long size,
            String type,
            int expires,
            @JsonProperty("owner") String owner
    ) {}
}