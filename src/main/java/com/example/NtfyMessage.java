package com.example;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.jetbrains.annotations.NotNull;

@JsonIgnoreProperties(ignoreUnknown = true)
public record NtfyMessage(
        String id,
        Long time,
        String event,
        String topic,
        String message) {

    @Override
    @NotNull
    public String toString(){
        return message != null ? message : String.format("NtfyMessage[event=%s]", event);
    }
}
