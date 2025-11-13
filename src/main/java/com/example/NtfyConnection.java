package com.example;

import java.io.File;
import java.util.function.Consumer;

public interface NtfyConnection {
    boolean send(String message);
    boolean sendFile(File file, String filename);
    boolean sendFileFromUrl(String fileUrl, String filename);
    void receive(Consumer<NtfyMessageDto> messageHandler);
}