package com.example;

import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {


    public String sentMessage = null;


    public Consumer<NtfyMessageDto> receivedHandler = null;

    @Override
    public boolean send(String message) {
        this.sentMessage = message; // Fångar meddelandet
        return true;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> messageHandler) {
        this.receivedHandler = messageHandler; // Fångar hanteringsfunktionen
    }


    public void simulateMessageArrival(String message, long timestamp) {
        if (receivedHandler != null) {
            // Skapa ett fejkat DTO och skicka det till den fångade hanteraren (Consumer)
            NtfyMessageDto fakeDto = new NtfyMessageDto(
                    "fake-id", timestamp, "message", "mytopic", message
            );
            receivedHandler.accept(fakeDto);
        }
    }
}