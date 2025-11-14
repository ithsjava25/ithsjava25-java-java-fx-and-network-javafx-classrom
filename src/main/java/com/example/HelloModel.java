package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;

/**
 * Model layer: encapsulates application data and business logic.
 */
public class HelloModel {
    /**
     * Returns a greeting based on the current Java and JavaFX versions.
     */

    private final NtfyConnection connection;
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private String lastSentMessage;

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

    public String getMessageToSend() {
        return messageToSend.get();
    }

    public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }

    public void sendMessage() {
        lastSentMessage = messageToSend.get();
        connection.send(lastSentMessage);
    }

    public void receiveMessage() {
        connection.receive(m -> {
            if (m.message().equals(lastSentMessage)) {
                return;
            }
            Platform.runLater(() -> messages.add(m));
        });
    }
}









