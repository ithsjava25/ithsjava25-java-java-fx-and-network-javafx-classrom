package com.example;

import java.io.File;
import java.util.function.Consumer;

/**
 * Test-dubbel (Spy) för att testa HelloModel utan att faktiskt skicka något till ntfy-servern.
 */
public class NtfyConnectionSpy implements NtfyConnection {
    public String lastSentMessage;
    public File lastSentImage;
    public String lastClientId;
    public Consumer<NtfyMessageDto> messageHandler;

    @Override
    public void send(String message) {
        this.lastSentMessage = message;
        System.out.println("🧪 Spy: send() called with -> " + message);
    }

    @Override
    public boolean sendImage(File imageFile, String clientId) {
        this.lastSentImage = imageFile;
        this.lastClientId = clientId;
        System.out.println("🧪 Spy: sendImage() called with -> " + imageFile.getName() + " | clientId=" + clientId);
        return true; // låtsas att det alltid lyckas
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler = messageHandler;
        System.out.println("🧪 Spy: receive() handler registered");
    }

    /**
     * Hjälpmetod för att simulera inkommande meddelanden till modellen.
     */
    public void simulateIncomingMessage(String content) {
        if (messageHandler != null) {
            var dto = new NtfyMessageDto(
                    "test-id",
                    System.currentTimeMillis(),
                    "message",
                    "MartinsTopic",
                    content
            );
            System.out.println("🧪 Spy: simulateIncomingMessage() -> " + content);
            messageHandler.accept(dto);
        } else {
            System.out.println("⚠️ Spy: simulateIncomingMessage() called before receive() was registered");
        }
    }
}
