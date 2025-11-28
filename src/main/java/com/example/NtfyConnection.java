package com.example;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface NtfyConnection {
    boolean send (String message);

    void receive (Consumer<NtfyMessageDto> messageHandler);

    boolean sendFile(Path filePath) throws FileNotFoundException;
}
