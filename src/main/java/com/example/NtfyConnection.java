package com.example;

import java.io.File;
import java.util.function.Consumer;

public interface NtfyConnection {

    /**
     * Sends a message to ntfy. The message can be plain text or a JSON/Markdown string containing links or formatting.
     *
     * @param jsonMessage the message to send
     */
    void send(String jsonMessage);

    /**
     * Uploads an image and notifies the server.
     *
     * @param imageFile the image file to send
     * @param clientId the identifier of the sending client
     * @return true if the image was successfully uploaded and the notification sent, false otherwise (e.g., network error)
     */
    boolean sendImage(File imageFile, String clientId);

    /**
     * Registers a consumer to receive incoming messages from ntfy.
     * The consumer is called for each incoming message.
     *
     * @param consumer the handler for incoming messages
     * @implNote Only one consumer should be registered per connection. Calling receive multiple times may overwrite the previous consumer.
     * @implNote Messages from the client itself (matching the same clientId) may be ignored depending on the implementation.
     */
    void receive(Consumer<NtfyMessageDto> consumer);

    /**
     * Stops receiving messages. After calling this method, the consumer passed to {@link #receive(Consumer)} may no longer be called.
     * Safe to call multiple times.
     */
    void stopReceiving();
}
