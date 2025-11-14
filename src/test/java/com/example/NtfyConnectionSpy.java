package com.example;

import java.util.function.Consumer;

/**
 * Spy for testing NtfyConnection: captures sent messages and simulates incoming ones.
 */
public class NtfyConnectionSpy implements NtfyConnection{

    String message;
    Consumer<NtfyMessageDto> handler;

    /** Captures message and calls callback with true on a background thread. */
    @Override
    public void send(String message, Consumer<Boolean> callback) {
        this.message = message;
        new Thread(() -> callback.accept(true)).start();
    }


    /** Saves the handler for incoming messages. */
    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.handler = messageHandler;
    }

    /** Triggers the saved handler with the given message, if there is any. */
    public void simulateIncoming(NtfyMessageDto msg) {
        if (handler != null) handler.accept(msg);
    }
}