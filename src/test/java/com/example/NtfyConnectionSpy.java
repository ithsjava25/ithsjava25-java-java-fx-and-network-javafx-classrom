package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {
    String message;
    Consumer<NtfyMessageDto> consumer;

    @Override
    public boolean send(String message) {
        this.message = message;
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> consumer) {
        this.consumer = consumer;
    }

    public void simulateIncomingMessage(NtfyMessageDto message) {
        if (consumer != null) {
            consumer.accept(message);
        }
    }

    }
