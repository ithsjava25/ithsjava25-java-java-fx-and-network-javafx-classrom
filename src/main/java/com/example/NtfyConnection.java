package com.example;

import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public interface NtfyConnection {

    boolean send(String message);

    void receive(Consumer<NtfyMessageDto> messageHandler);

    List<NtfyMessageDto> fetchHistory();

    boolean sendFile(Path path) throws FileNotFoundException;
}
