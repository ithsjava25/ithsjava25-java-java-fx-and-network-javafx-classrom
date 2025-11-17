package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {

    public boolean sendMessage(String message);

    public void receiveMessage(Consumer<NtfyMessageDto> messageHandler);
}
