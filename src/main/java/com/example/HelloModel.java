package com.example;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.function.Consumer;

import static com.example.FxUtils.runOnFx;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty("");

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        startReceiving();
    }

    private void startReceiving() {
        connection.receive(incoming -> {
            if (!isValidMessage(incoming)) {
                return;
            }
            runOnFx(() -> messages.add(incoming));
        });
    }

    private boolean isValidMessage(NtfyMessageDto message) {
        return message != null
                && message.message() != null
                && !message.message().isBlank();
    }

    public void sendMessageAsync(Consumer<Boolean> callback) {
        String msg = messageToSend.get();
        if (msg == null || msg.isBlank()) {
            callback.accept(false);
            return;
        }

        connection.send(msg, success -> {
            if (success) {
                runOnFx(() -> {
                    if (msg.equals(messageToSend.get())) {
                        messageToSend.set("");
                    }
                });
            }
            callback.accept(success);
        });
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

    public void setMessageToSend(String value) {
        messageToSend.set(value);
    }

    public String getGreeting() {
        return "Welcome to ChatApp";
    }
}