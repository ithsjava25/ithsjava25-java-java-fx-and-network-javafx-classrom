package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class NtfyConnectionSpy implements NtfyConnection {


    public String message;            // senaste textmeddelandet
    public String fileName;           // senaste filnamnet
    public byte[] fileData;           // senaste fildatat

    // För att kunna simulera "receive"
    private final List<Consumer<NtfyMessageDto>> receivers = new ArrayList<>();

    @Override
    public boolean send(String message) {
        this.message = message;
        return true;
    }

    @Override
    public void sendFile(String fileName, byte[] data) {
        this.fileName = fileName;
        this.fileData = data;
    }

    @Override
    public void receive(Consumer<NtfyMessageDto> handler) {
        // Spara callback för senare användning
        receivers.add(handler);
    }

    /**
     * Hjälpmetod för test: trigga ett fejk-meddelande
     */
    public void simulateIncomingMessage(NtfyMessageDto dto) {
        receivers.forEach(handler -> handler.accept(dto));
    }

    /**
     * Reset mellan tester (om du vill)
     */
    public void reset() {
        message = null;
        fileName = null;
        fileData = null;
        receivers.clear();
    }
}
