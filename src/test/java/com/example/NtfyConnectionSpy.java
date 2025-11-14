package com.example;

import java.io.File;
import java.util.function.Consumer;

/**
 * Test-double för HelloModel.
 * Simulerar skickande och mottagning av meddelanden utan HTTP.
 */
public class NtfyConnectionSpy implements NtfyConnection {

    public String lastSentMessage;
    public File lastSentImage;
    public String lastClientId;
    public Consumer<NtfyMessageDto> messageHandler;

    @Override
    public void send(String message) {
        this.lastSentMessage = message;
        this.lastClientId = null; // Plain text, ingen clientId
        System.out.println("🧪 Spy send(): " + message);
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
     * Simulerar inkommande meddelande.
     */
    public void simulateIncomingMessage(String message, String imageUrl, String clientId) {
        if (messageHandler != null) {
            long now = System.currentTimeMillis() / 1000;
            NtfyMessageDto dto = new NtfyMessageDto(
                    "test-id",
                    now,
                    "message",
                    HelloModel.DEFAULT_TOPIC,
                    message,
                    null,
                    imageUrl,
                    clientId
            );
            messageHandler.accept(dto);
        }
    }
}
