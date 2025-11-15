package com.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class FakeNtfyConnection implements NtfyConnection {
    private final List<String> sentMessages = new ArrayList<>();
    private final List<File> sentFiles = new ArrayList<>();
    private Consumer<NtfyMessageDto> messageHandler;
    private boolean shouldSucceed = true;

    /**
     * Sends a text message
     * @param message the message text to send
     * @return true if operation should succeed (based on shouldSucceed flag), false otherwise
     */
    @Override
    public boolean send(String message) {
        sentMessages.add(message);
        return shouldSucceed;
    }

    /**
     * Sends a file
     * @param file the file to send, must exist and not be null
     * @return true if file exists and operation should succeed, false otherwise
     */
    @Override
    public boolean sendFile(File file) {
        if (file != null && file.exists()) {
            sentFiles.add(file);
            return shouldSucceed;
        }
        return false;
    }

    /**
     * Sets up the message handler for receiving incoming messages
     * @param handler the consumer that will process incoming NtfyMessageDto objects
     */
    @Override
    public void receive(Consumer<NtfyMessageDto> handler) {
        this.messageHandler = handler;
    }

    /**
     * Simulates an incoming message for testing purposes
     * @param message the message DTO to simulate as incoming
     */
    public void simulateIncomingMessage(NtfyMessageDto message) {
        if (messageHandler != null) {
            messageHandler.accept(message);
        }
    }

    /**
     * Gets all sent messages for verification
     * @return a copy of the list of sent messages
     */
    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    /**
     * Gets all sent files for verification
     * @return a copy of the list of sent files
     */
    public List<File> getSentFiles() {
        return new ArrayList<>(sentFiles);
    }

    /**
     * Configures whether operations should succeed or fail
     * @param shouldSucceed true to make operations return success, false to make them fail
     */
    public void setShouldSucceed(boolean shouldSucceed) {
        this.shouldSucceed = shouldSucceed;
    }

    /**
     * Clears all stored state and resets the connection
     */
    public void clear() {
        sentMessages.clear();
        sentFiles.clear();
        messageHandler = null;
    }
}