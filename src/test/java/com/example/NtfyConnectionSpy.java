package com.example;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {

    String message;

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
    }

    @Override
    public List<NtfyMessageDto> fetchHistory() {
        return Collections.emptyList();
    }
}
