package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection{

    String message;

    @Override
    public boolean sendMessage(String message) {
        this.message = message;
        return true;
    }

    @Override
    public void receiveMessage(Consumer<NtfyMessageDto> messageHandler) {

    }

}
