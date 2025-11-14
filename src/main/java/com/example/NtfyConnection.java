package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NtfyConnection {
    CompletableFuture<Void> send(String message);
    void receive(Consumer<NtfyMessageDto> messageHandler);
}