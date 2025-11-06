package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;

    @Override
    public CompletableFuture<Void> send(String message) {
        this.message = message;
//        return true;
        return null;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {

    }
}
