package com.example;

import java.io.File;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface NtfyConnection {
    boolean send(String message);           // ✅ Den här måste finnas
    void receive(Consumer<NtfyMessageDto> messageHandler);
    boolean sendFile(File file);
}