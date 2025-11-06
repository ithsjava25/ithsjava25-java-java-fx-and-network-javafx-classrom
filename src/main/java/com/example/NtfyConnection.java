package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NtfyConnection {

    public CompletableFuture<Void> send(String message);

    public void receive(Consumer<NtfyMessageDto> messageHandler);
}
