package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.function.Consumer;

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

    public String getGreeting() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        return "Welcome to ChatApp, made in JavaFX " + javafxVersion + ", running on Java " + javaVersion + ".";
    }

    public void sendMessage() {
        String msg = messageToSend.get();
        if (msg == null || msg.isBlank()) {
            return;
        }
        connection.send(msg);
    }

    public void sendMessageAsync(Consumer<Boolean> callback) {
        String msg = messageToSend.get();
        if (msg == null || msg.isBlank()) {
            callback.accept(false);
            return;
        }

        try {
            boolean success = connection.send(msg);
            callback.accept(success);
        } catch (Exception e) {
            callback.accept(false);
        }
    }

    private void receiveMessage() {
        connection.receive(message -> {
            if (message == null || message.message() == null || message.message().isBlank()) {
                return;
            }

            if (System.getProperty("java.awt.headless", "false").equals("true")) {
                messages.add(message); // lägg direkt i listan
                return;
            }

            try {
                if (Platform.isFxApplicationThread()) {
                    messages.add(message);
                } else {
                    Platform.runLater(() -> messages.add(message));
                }
            } catch (UnsupportedOperationException | IllegalStateException e) {
                messages.add(message);
            }
        });
    }
}