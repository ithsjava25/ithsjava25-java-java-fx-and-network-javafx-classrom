package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {
    void send(String message);
    void receive(Consumer<NtfyMessageDto> consumer);
}

