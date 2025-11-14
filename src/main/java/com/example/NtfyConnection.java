package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public interface NtfyConnection {

    void send(String message, Consumer<Boolean> callback);

    void receive(Consumer<NtfyMessageDto> messageHandler);

    default String getCurrentTopic() {
        return "mytopic";
    }

    default void setCurrentTopic(String topic) {

    }

    default String getUserId() {
        return "unknown";
    }
}