package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NtfyConnection {

    public CompletableFuture<Boolean> send(String message);

    public void receive(Consumer<NtfyMessageDto> messageHandler);

}