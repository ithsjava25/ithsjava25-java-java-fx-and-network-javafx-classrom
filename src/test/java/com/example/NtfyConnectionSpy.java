package com.example;

import java.io.File;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {
    public String message;
    public File file;
    public int sendCallCount = 0;
    public int sendFileCallCount = 0;
    private Consumer<NtfyMessageDto> messageHandler;
    private boolean simulateReceive = false;

    @Override
    public boolean send(String message) {
        this.message = message;
        this.sendCallCount++;
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler = messageHandler;
        if (simulateReceive) {
            triggerTestMessage();
        }
    }

    @Override
    public boolean sendFile(File file) {
        this.file = file;
        this.sendFileCallCount++;
        return true;
    }

    // Helper method to trigger a test message
    public void triggerTestMessage() {
        if (messageHandler != null) {
            NtfyMessageDto testMessage = new NtfyMessageDto(
                    "test-id",
                    System.currentTimeMillis(),
                    "message",
                    "mytopic",
                    "Test message from spy",
                    null,
                    null
            );
            messageHandler.accept(testMessage);
        }
    }

    // Helper methods for testing
    public void reset() {
        this.message = null;
        this.file = null;
        this.sendCallCount = 0;
        this.sendFileCallCount = 0;
        this.messageHandler = null;
    }

    public void setSimulateReceive(boolean simulateReceive) {
        this.simulateReceive = simulateReceive;
    }
}