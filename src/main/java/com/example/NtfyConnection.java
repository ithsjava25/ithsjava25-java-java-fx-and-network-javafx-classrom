package com.example;

import java.util.function.Consumer;

public interface NtfyConnection {

    /**
     * Skickar ett textmeddelande till servern.
     *
     * @param message meddelandet som ska skickas
     * @return true om det lyckades, false annars
     */
    boolean send(String message);

    /**
     * Registrerar en mottagare som hanterar inkommande meddelanden.
     *
     * @param messageHandler funktion som hanterar inkommande NtfyMessageDto
     */
    void receive(Consumer<NtfyMessageDto> messageHandler);

    /**
     * Skickar en fil till servern.
     *
     * @param filename namnet på filen
     * @param data innehållet i filen som byte-array
     */
    void sendFile(String filename, byte[] data);
}




