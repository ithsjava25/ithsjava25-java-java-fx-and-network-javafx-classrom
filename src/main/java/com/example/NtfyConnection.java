package com.example;

import java.io.File;
import java.util.function.Consumer;

public interface NtfyConnection {
    /**
     * Skickar ett JSON-meddelande till ntfy
     * Kan innehålla text eller markdown med bild-länk
     */
    void send(String jsonMessage);

    boolean sendImage(File imageFile, String clientId);

    /**
     * Tar emot meddelanden från ntfy via JSON
     */
    void receive(Consumer<NtfyMessageDto> consumer);

    /**
     * Stoppar mottagartråden
     */
    void stopReceiving();
}

