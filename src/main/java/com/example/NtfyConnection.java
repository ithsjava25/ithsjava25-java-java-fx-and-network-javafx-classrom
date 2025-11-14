package com.example;
import java.util.function.Consumer;

public interface NtfyConnection {
    boolean send (String message);
    void receive (Consumer<NtfyMessageDto> messageHandler);
}
