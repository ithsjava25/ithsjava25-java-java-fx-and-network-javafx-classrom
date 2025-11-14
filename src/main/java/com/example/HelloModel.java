package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        startReceivingMessages();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public String getMessageToSend() {
        return messageToSend.get();
    }

    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }

    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */
    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Welcome to JavaFX Chat App " + javafxVersion + ", running on Java " + javaVersion + ".";
    }

    public boolean sendMessage() {
        String message = getMessageToSend();
        if (message == null || message.trim().isEmpty()) {
            return false;
        }
        return connection.send(message.trim());
    }

    public boolean sendFile(File file) {
        if (file == null || !file.exists()) {
            return false;
        }
        return connection.sendFile(file);
    }

    private void startReceivingMessages() {
        connection.receive(this::addMessageToUI);
    }

    private void addMessageToUI(NtfyMessageDto message) {
        // Check if we're on JavaFX application thread, if not use Platform.runLater
        if (Platform.isFxApplicationThread()) {
            messages.add(message);
        } else {
            Platform.runLater(() -> messages.add(message));
        }
    }

    // Test helper method - package private for testing
    void addTestMessage(NtfyMessageDto message) {
        // Direct add for testing (bypasses Platform.runLater)
        messages.add(message);
    }
}