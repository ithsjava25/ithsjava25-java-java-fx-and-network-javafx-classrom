package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {
    public String message;
    private Consumer<NtfyMessageDto> handler;

    @Override
    public void send(String message, Consumer<Boolean> callback) {
        this.message = message;
        callback.accept(true);
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> h) {
        this.handler = h;
    }

    public void simulateIncoming(NtfyMessageDto msg) {
        if (handler != null) handler.accept(msg);
    }
}