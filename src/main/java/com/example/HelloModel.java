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

    private String messageToSend = "";

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;

        messages.add(new NtfyMessageDto("init", 0, "message", "mytopic", "Initial message"));

        receiveMessages();
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    public String getMessageToSend() {
        return messageToSend;
    }

    public void setMessageToSend(String messageToSend) {
        this.messageToSend = messageToSend;
    }

    /**
     * Ny version av sendMessage() som TVÅ av dina tester anropar
     */
    public void sendMessage() {
        if (messageToSend != null && !messageToSend.isBlank()) {
            connection.send(messageToSend);
        }
        messageToSend = "";
    }

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

    public String getGreeting() {
        return "Hello, JavaFX!";
    }
}
