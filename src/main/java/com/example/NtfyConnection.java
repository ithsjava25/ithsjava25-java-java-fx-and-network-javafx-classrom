package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Consumer;

public interface NtfyConnection {

    public boolean send(String message);

    public void receive(Consumer<NtfyMessageDto> messageHandler);

    public boolean sendFile(File file) throws FileNotFoundException;
}
