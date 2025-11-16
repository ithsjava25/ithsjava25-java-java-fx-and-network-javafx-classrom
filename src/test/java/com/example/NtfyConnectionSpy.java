package com.example;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    @Override
    public void recieve(Consumer<NtfyMessageDto> messageHandler) {

    }

    @Override
    public boolean sendFile(Path filePath) throws FileNotFoundException {
        return false;
    }
}
