package com.example;

import java.io.File;
import java.util.function.Consumer;

/**
 * Test-dubbel för HelloModel.
 * Tar emot och skickar meddelanden utan riktig HTTP.
 */
public class NtfyConnectionSpy implements NtfyConnection {

    public String lastSentMessage;
    public File lastSentImage;
    public String lastClientId;
    public Consumer<NtfyMessageDto> messageHandler;

    @Override
    public void send(String jsonMessage) {
        this.lastSentMessage = jsonMessage;
        System.out.println("🧪 Spy send(): " + jsonMessage);
    }

    @Override
    public boolean sendImage(File imageFile, String clientId) {
        this.lastSentImage = imageFile;
        this.lastClientId = clientId;
        System.out.println("🧪 Spy sendImage(): " + imageFile.getName() + " | clientId=" + clientId);
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler = messageHandler;
        System.out.println("🧪 Spy receive() handler registered");
    }

    @Override
    public void stopReceiving() { }

    /**
     * Simulerar ett inkommande JSON-meddelande.
     */
    public void simulateIncomingMessage(String json) {
        if (messageHandler != null) {
            long now = System.currentTimeMillis() / 1000;

            NtfyMessageDto dto = new NtfyMessageDto(
                    "test-id",
                    now,
                    "message",
                    HelloModel.DEFAULT_TOPIC,
                    json.contains("\"message\"") ? json.replaceAll(".*\"message\":\"([^\"]+)\".*", "$1") : null,
                    null,
                    json.contains("\"imageUrl\"") ? json.replaceAll(".*\"imageUrl\":\"([^\"]+)\".*", "$1") : null
            );
            messageHandler.accept(dto);
        }
    }
}
