package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {
    String message;
    private Consumer<NtfyMessageDto> messageHandler;

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void simulateIncoming(NtfyMessageDto msg) {
        if (messageHandler != null) {
            messageHandler.accept(msg);
        }
    }
}