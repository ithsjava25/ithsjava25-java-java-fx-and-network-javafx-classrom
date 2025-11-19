package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    private String message;
    private boolean receiveCalled = false;
    private Consumer<NtfyMessageDto> lastHandler;

    @Override
    public boolean sendMessage(String message) {
        this.message = message;
        return true;
    }

    @Override
    public void receiveMessage(Consumer<NtfyMessageDto> messageHandler) {
        this.receiveCalled = true;
        this.lastHandler = messageHandler;
    }

    public String message() {
        return message;
    }

    public boolean wasReceiveCalled() {
        return receiveCalled;
    }

    public void triggerReceive(NtfyMessageDto dto) {
        if (lastHandler != null) {
            lastHandler.accept(dto);
        }
    }
}
