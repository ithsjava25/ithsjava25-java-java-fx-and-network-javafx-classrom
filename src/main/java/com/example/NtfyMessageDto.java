package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessageDto(String id, Long time, String event, String topic, String message) {

    public String formattedTime() {
        if (time == null) return "";
        return DateTimeFormatter.ofPattern("HH:mm")
                .format(Instant.ofEpochSecond(time)
                        .atZone(ZoneId.of("Europe/Stockholm")));

    }

}
