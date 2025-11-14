package com.example;

import java.nio.file.Path;
import java.util.function.Consumer;

public interface NtfyConnection {

    public boolean send(String message);

    public void receive(Consumer<NtfyMessageDto> messageHandler);
    boolean sendFile(Path filePath);

}