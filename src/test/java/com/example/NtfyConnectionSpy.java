package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {
    String message;

    @Override
    public boolean send(String message) {
        this.message = message;
        return false;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> consumer) {

    }
}
