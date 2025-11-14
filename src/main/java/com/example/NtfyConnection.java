package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Interface for sending and receiving messages via a notification service.
 */
public interface NtfyConnection {

    void send(String message, Consumer<Boolean> callback);

    /**
     * Receives messages from the service.
     * @param messageHandler called for each received message
     */
    void receive(Consumer<NtfyMessageDto> messageHandler);

    /** Returns the current topic. Default is "mytopic". */
    default String getCurrentTopic() {
        return "mytopic";
    }

    /** Sets the current topic. Default does nothing. */
    default void setCurrentTopic(String topic) {

    }

    /** Returns the user ID. Default is "unknown". */
    default String getUserId() {
        return "unknown";
    }
}