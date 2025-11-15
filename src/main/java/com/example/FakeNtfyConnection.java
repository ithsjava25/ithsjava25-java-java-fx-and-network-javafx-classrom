package com.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FakeNtfyConnection implements NtfyConnection {
    private final List<String> sentMessages = new ArrayList<>();
    private final List<File> sentFiles = new ArrayList<>();
    private Consumer<NtfyMessageDto> messageHandler;
    private boolean shouldSucceed = true;

    @Override
    public boolean send(String message) {
        sentMessages.add(message);
        return shouldSucceed;
    }

    @Override
    public boolean sendFile(File file) {
        if (file != null && file.exists()) {
            sentFiles.add(file);
            return shouldSucceed;
        }
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> handler) {
        this.messageHandler = handler;
    }

    // Test helper methods
    public void simulateIncomingMessage(NtfyMessageDto message) {
        if (messageHandler != null) {
            messageHandler.accept(message);
        }
    }

    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    public List<File> getSentFiles() {
        return new ArrayList<>(sentFiles);
    }

    public void setShouldSucceed(boolean shouldSucceed) {
        this.shouldSucceed = shouldSucceed;
    }

    public void clear() {
        sentMessages.clear();
        sentFiles.clear();
        messageHandler = null;
    }
}