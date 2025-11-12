package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection{

    String message;
    Consumer<NtfyMessageDto> messageHandler;


    @Override
    public CompletableFuture<Boolean> send(String message) {
        this.message = message;
        return CompletableFuture.completedFuture(true);
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void simulateIncomingMessage(NtfyMessageDto message) {
        if (messageHandler != null) {
            messageHandler.accept(message);
        }
    }
}