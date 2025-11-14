package com.example;

import java.nio.file.Path;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;
    Path filePath; // valfritt: lagra filen som skickas

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    @Override
    public boolean sendFile(Path filePath) {
        this.filePath = filePath; // spara för verifiering i test
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        // tom i spy
    }
}
