package com.example;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A fake implementation of {@link NtfyConnection} for testing purposes.
 * Stores sent messages and files in memory and simulates incoming messages.
 */
public class FakeNtfyConnection implements NtfyConnection {

    private final List<String> sentMessages = new ArrayList<>();
    private final List<File> sentFiles = new ArrayList<>();
    private Consumer<NtfyMessageDto> messageHandler;
    private boolean shouldSucceed = true;

    /**
     * Sends a text message.
     * Stores the message in memory and returns success based on {@link #shouldSucceed}.
     *
     * @param message the message to send
     * @return true if operation succeeds, false otherwise
     */
    @Override
    public boolean send(String message) {
        sentMessages.add(Objects.requireNonNull(message));
        return shouldSucceed;
    }

    /**
     * Sends a file.
     * Stores the file in memory if it exists and returns success based on {@link #shouldSucceed}.
     *
     * @param file the file to send
     * @return true if file exists and operation succeeds, false otherwise
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
     * Registers a message handler to receive incoming messages.
     * Only the last registered handler will be active.
     *
     * @param handler the consumer that handles incoming messages
     */
    @Override
    public void receive(Consumer<NtfyMessageDto> handler) {
        this.messageHandler = Objects.requireNonNull(handler);
    }

    /**
     * Simulates an incoming message.
     * Calls the registered handler if present.
     *
     * @param message the message to simulate
     */
    public void simulateIncomingMessage(NtfyMessageDto message) {
        if (messageHandler != null) {
            messageHandler.accept(message);
        }
    }

    /**
     * Returns a copy of all sent messages for verification.
     *
     * @return list of sent messages
     */
    public List<String> getSentMessages() {
        return new ArrayList<>(sentMessages);
    }

    /**
     * Returns a copy of all sent files for verification.
     *
     * @return list of sent files
     */
    public List<File> getSentFiles() {
        return new ArrayList<>(sentFiles);
    }

    /**
     * Configures whether operations should succeed.
     *
     * @param shouldSucceed true for success, false for failure
     */
    public void setShouldSucceed(boolean shouldSucceed) {
        this.shouldSucceed = shouldSucceed;
    }

    /**
     * Clears all stored messages, files, and the message handler.
     */
    public void clear() {
        sentMessages.clear();
        sentFiles.clear();
        messageHandler = null;
    }
}
