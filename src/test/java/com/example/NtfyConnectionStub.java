package com.example;

import java.util.function.Consumer;

public class NtfyConnectionStub implements NtfyConnection {

    private Consumer<NtfyMessageDto> messageHandler;

    @Override
    public boolean send(String topic, String message) {
        return true;
    }

    @Override
    public void receive(String topic, Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler = messageHandler;
    }

    public void simulateIncomingMessage(NtfyMessageDto message) {
        if (messageHandler != null) {
            messageHandler.accept(message);
        }
    }
}
