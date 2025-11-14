package com.example;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        receiveMessages();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /**
     * Skickar ett textmeddelande via connection.
     */
    public void sendMessage(String text) {
        if (text != null && !text.isBlank()) {
            connection.send(text);
        }
    }

    /**
     * Skickar en fil via connection.
     */
    public void sendFile(File file) {
        try {
            byte[] data = Files.readAllBytes(file.toPath());
            connection.sendFile(file.getName(), data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Tar emot meddelanden från backend och lägger till i ObservableList.
     */
    public void receiveMessages() {
        connection.receive(m -> Platform.runLater(() -> messages.add(m)));
    }

    /**
     * Valfri hälsningssträng för label.
     */
    public String getGreeting() {
        return "Hello, JavaFX!";
    }
}

