package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.function.Consumer;

public interface NtfyConnection {

    String getTopic();

    boolean send(String message, String topic);

    boolean sendFile(File file, String topic);

    void connect(String topic, Consumer<NtfyMessageDto> messageHandler);

    //public boolean send(String message);

    public void receive(Consumer<NtfyMessageDto> messageHandler);

    //public boolean sendFile(File file) throws FileNotFoundException;
}
