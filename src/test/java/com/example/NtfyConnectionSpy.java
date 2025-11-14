package com.example;

import java.io.File;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    public String lastSentMessage;
    public File lastSentImage;
    public String lastClientId;
    public Consumer<NtfyMessageDto> messageHandler;

    @Override
    public void send(String message) {
        this.lastSentMessage = message;
    }

    @Override
    public boolean sendImage(File imageFile, String clientId) {
        this.lastSentImage = imageFile;
        this.lastClientId = clientId;
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    public void stopReceiving() { }

    // ny hjälpfunktion för JSON-strängar
    public void simulateIncomingMessage(String json) {
        if (messageHandler != null) {
            NtfyMessageDto dto = new NtfyMessageDto(
                    "test-id",
                    System.currentTimeMillis(),
                    "message",
                    "MartinsTopic",
                    json.contains("\"message\"") ? json.replaceAll(".*\"message\":\"([^\"]+)\".*", "$1") : null,
                    null,
                    json.contains("\"imageUrl\"") ? json.replaceAll(".*\"imageUrl\":\"([^\"]+)\".*", "$1") : null
            );
            messageHandler.accept(dto);
        }
    }
}
