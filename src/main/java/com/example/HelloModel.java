package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;


public class HelloModel {
    private final NtfyConnection connection;

    private final ObservableList<String> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessage();
    }


    public ObservableList<String> getMessages() {
        return messages;
    }

    public void setMessageToSend(String message) {
        this.messageToSend.set(message);
    }


    public void sendMessage() {
        connection.send(messageToSend.get());
    }

    public void sendFileToServer(File file) throws IOException {
        connection.sendFile(file.toPath());
    }

    public void receiveMessage() {
        connection.recieve(m-> Platform.runLater(()-> messages.add(m.message())));
    }

}

