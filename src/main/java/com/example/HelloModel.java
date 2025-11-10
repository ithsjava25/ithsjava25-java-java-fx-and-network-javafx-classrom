package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.function.Consumer;

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
        return "Welcome to ChatApp, made in JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }

    public void sendMessage() {
        String msg = messageToSend.get();
        if (msg == null || msg.isBlank()) {
            System.out.println("Nothing to send!");
            return;
        }
        connection.send(msg);
    }


    public void receiveMessage() {
        connection.receive(m -> Platform.runLater(() -> messages.add(m)));
    }

    /**
     * For testing - allows setting a callback for received messages
     */
    public void setOnMessageReceived(Consumer<String> onMessageReceived) {
        connection.receive(ntfyMessage -> {
            //Test callback
            onMessageReceived.accept(ntfyMessage.message());
            Platform.runLater(() -> messages.add(ntfyMessage));
        });
    }
}
