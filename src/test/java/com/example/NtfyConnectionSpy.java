package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection{

    String message;
    Consumer<NtfyMessageDto> handler;

    @Override
    public void send(String message, Consumer<Boolean> callback) {
        this.message = message;
        new Thread(() -> callback.accept(true)).start();
    }


    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.handler = messageHandler;
    }

    public void simulateIncoming(NtfyMessageDto msg) {
        if (handler != null) handler.accept(msg);
    }
}