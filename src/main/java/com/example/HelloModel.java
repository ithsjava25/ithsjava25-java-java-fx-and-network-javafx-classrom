package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.UUID;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */

    private final NtfyConnection connection;
    private final StringProperty messageToSend = new SimpleStringProperty("");
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final String clientId = UUID.randomUUID().toString();

    //Konstuktor för prod
    public HelloModel() {
        this.connection = new NtfyConnectionImpl();
        receiveMessage();
    }

    //Konstuktor för test
    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }

    public void sendMessage() {
        String text = messageToSend.get().trim();
        if (text.isEmpty()) return;

        String payload = clientId + "::" + text;
        connection.send(payload);

        messageToSend.set("");
    }

    public void receiveMessage() {
        connection.receive(msg -> {
            String incoming = msg.message();

            if (incoming.startsWith(clientId + "::")) {
                return;
            }

            Platform.runLater(() -> messages.add(msg));
        });
    }
}









