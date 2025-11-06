package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;
    public Consumer<NtfyMessageDto> messageHandler;

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.messageHandler=messageHandler;

    }

    @Override
    public boolean sendFile(File file) throws FileNotFoundException {
        return false;
    }
}
