package com.example;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.function.Consumer;

public interface NtfyConnection {

    boolean send(String message);

    void recieve(Consumer<NtfyMessageDto> messageHandler);

    boolean sendFile(Path filePath) throws FileNotFoundException;
}
