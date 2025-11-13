package com.example;

import java.io.File;
import java.util.function.Consumer;

public interface NtfyConnection {
    boolean send(String message);
    void receive(Consumer<NtfyMessageDto> messageHandler);
    boolean sendFile(File file);
    // boolean sendFileFromUrl(String url); // Optional: uncomment if you want to implement this
}