package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    private String lastMessage;
    private String lastFileName;
    private byte[] lastFileData;

    @Override
    public boolean send(String message) {
        this.lastMessage = message;
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        // tom, används inte i detta test
    }

    public void sendFile(String fileName, byte[] data) {
        this.lastFileName = fileName;
        this.lastFileData = data;
    }

    // Getters för tester
    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastFileName() {
        return lastFileName;
    }

    public byte[] getLastFileData() {
        return lastFileData;
    }
}
