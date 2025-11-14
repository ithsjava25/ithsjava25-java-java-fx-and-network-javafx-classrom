package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.function.Consumer;

import static com.example.FxUtils.runOnFx;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    private final StringProperty messageToSend = new SimpleStringProperty();
    private final StringProperty currentTopic = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        this.currentTopic.set(connection.getCurrentTopic());
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

    public String getCurrentTopic() {
        return currentTopic.get();
    }

    public StringProperty currentTopicProperty() {
        return currentTopic;
    }

    public void setCurrentTopic(String topic) {
        if (topic != null && !topic.isBlank()) {
            connection.setCurrentTopic(topic);
            this.currentTopic.set(topic);
            messages.clear();
            receiveMessage();
        }
    }

    public String getUserId() {
        return connection.getUserId();
    }

    public String getGreeting() {
        return "RuneChat";
    }

    public boolean canSendMessage() {
        String msg = messageToSend.get();
        return msg != null && !msg.isBlank();
    }

    public void sendMessageAsync(Consumer<Boolean> callback) {
        String msg = messageToSend.get();
        if (msg == null || msg.isBlank()) {
            System.out.println("Nothing to send!");
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
                callback.accept(true);
            } else {
                System.out.println("Failed to send message!");
                callback.accept(false);
            }
        });
    }

    public void receiveMessage() {
        connection.receive(m -> {
            if (m == null || m.message() == null || m.message().isBlank()) return;
            runOnFx(() -> messages.add(m));
        });
    }


}