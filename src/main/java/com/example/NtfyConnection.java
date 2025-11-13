package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {

    public boolean send(String topic, String message);

    public void receive(String topic, Consumer<NtfyMessageDto> messageHandler);

}