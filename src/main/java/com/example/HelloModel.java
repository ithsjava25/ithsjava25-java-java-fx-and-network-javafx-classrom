package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final NtfyConnection connection;

    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
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
        return "Hello, 404 java not found!";
    }

    public CompletableFuture<Boolean> sendMessage() {
        System.out.println("Meddelande att skicka: " + messageToSend.get());
        return connection.send(messageToSend.get());
    }

    public CompletableFuture<Boolean> sendFile(Path filePath) {
        return connection.sendFile(filePath);
    }

    public void receiveMessage() {
        connection.receive(m -> runOnFx(() -> messages.add(m)));
    }

    public void testAddMessage() {
        NtfyMessageDto test = new NtfyMessageDto("id123", System.currentTimeMillis(), "message", "mytopic", "Testmeddelande", null);
        runOnFx(() -> messages.add(test));
    }

    static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) task.run();
            else Platform.runLater(task);
        } catch (IllegalStateException notInitialized) {
            task.run();
        }
    }

}