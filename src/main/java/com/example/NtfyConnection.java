package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {

    public void send(String message);

    public void receive(Consumer<NtfyMessageDto> messageHandler);
}
