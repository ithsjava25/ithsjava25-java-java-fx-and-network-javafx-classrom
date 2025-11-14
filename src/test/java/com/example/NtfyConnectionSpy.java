package com.example;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;
    private Consumer<NtfyMessageDto> messageHandler;


    @Override
    public CompletableFuture<Void> send(String message) {
        this.message = message;
        return CompletableFuture.completedFuture(null);
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler = messageHandler;
    }


    public void simulateIncomingMessage(NtfyMessageDto messageDto){
        if (messageHandler != null)
            messageHandler.accept(messageDto);
    }
}