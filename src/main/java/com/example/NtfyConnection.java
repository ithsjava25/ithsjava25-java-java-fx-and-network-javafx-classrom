package com.example;

import java.io.File;
import java.util.function.Consumer;

public interface NtfyConnection {
    /**
     * Sends a text message to the NTFY topic
     * @param message the text message to send
     * @return true if the message was sent successfully, false otherwise
     */
    boolean send(String message);

    /**
     * Starts receiving messages from the NTFY topic
     * @param messageHandler the consumer callback that will process incoming messages
     */
    void receive(Consumer<NtfyMessageDto> messageHandler);

    /**
     * Sends a file attachment to the NTFY topic
     * @param file the file to send as an attachment
     * @return true if the file was sent successfully, false otherwise
     */
    boolean sendFile(File file);
}