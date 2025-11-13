package com.example;

import java.io.File;
import java.util.function.Consumer;

public interface NtfyConnection {
    void send(String message);
    boolean sendImage(File imageFile, String clientId);
    void receive(Consumer<NtfyMessageDto> consumer);
    void stopReceiving();
}

