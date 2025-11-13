package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;
    String topic;

    @Override
    public boolean send(String topic, String message) {
        this.topic = topic;
        this.message = message;
        return true;
    }

    @Override
    public void receive(String topic, Consumer<NtfyMessageDto> messageHandler) {
    }

}