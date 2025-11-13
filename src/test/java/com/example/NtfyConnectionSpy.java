package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {
    String message;  // För testverifiering
    private Consumer<NtfyMessageDto> messageHandler;

    @Override
    public void send(String message) {
        this.message = message;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler = messageHandler;
    }

    // Hjälpmetod för att simulera inkommande meddelanden
    public void simulateIncomingMessage(String content) {
        if (messageHandler != null) {
            var dto = new NtfyMessageDto(
                    "test-id",
                    System.currentTimeMillis(),
                    "message",
                    "MartinsTopic",
                    content
            );
            messageHandler.accept(dto);
        }
    }
}
