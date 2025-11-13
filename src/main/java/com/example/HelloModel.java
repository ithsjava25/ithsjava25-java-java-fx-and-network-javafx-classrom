package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final String clientId = java.util.UUID.randomUUID().toString();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage(); // start listening immediately
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
        this.messageToSend.set(message);
    }

    public void sendMessage(String message) {
        connection.send(message);
    }

    public void receiveMessage() {
        connection.receive(m -> {
            if (Platform.isFxApplicationThread()) {
                messages.add(m);
            } else {
                try {
                    Platform.runLater(() -> messages.add(m));
                } catch (IllegalStateException e) {
                    // Fångas i tester där JavaFX Toolkit inte är initierad
                    messages.add(m);
                }
            }
        });
    }


    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Hello, JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }
}
