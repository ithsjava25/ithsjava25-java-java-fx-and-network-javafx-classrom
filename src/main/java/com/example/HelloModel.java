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
    private final StringProperty messageToSend = new SimpleStringProperty("");

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
        startReceiving();
    }

    private void startReceiving() {
        connection.receive(incoming -> {
            if (incoming == null || incoming.message() == null || incoming.message().isBlank()) {
                return;
            }
            runOnFx(() -> messages.add(incoming));
        });
    }

    public void sendMessageAsync(Consumer<Boolean> callback) {
        String msg = messageToSend.get();
        if (msg == null || msg.isBlank()) {
            callback.accept(false);
            return;
        }

        try {
            connection.send(msg, success -> {
                if (success) {
                    runOnFx(() -> {
                        if (msg.equals(messageToSend.get())) {
                            messageToSend.set("");
                        }
                    });
                    callback.accept(true);
                } else {
                    callback.accept(false);
                }
            });
        } catch (Exception e) {
            // FÅNGA ALLA EXCEPTIONS HÄR!
            System.err.println("Exception during send: " + e.getMessage());
            callback.accept(false);
        }
    }

    private static void runOnFx(Runnable task) {
        try {
            if (Platform.isFxApplicationThread()) {
                task.run();
            } else {
                Platform.runLater(task);
            }
        } catch (Exception e) {
            task.run(); // fallback i tester
        }
    }

    public ObservableList<NtfyMessageDto> getMessages() { return messages; }
    public String getMessageToSend() { return messageToSend.get(); }
    public StringProperty messageToSendProperty() { return messageToSend; }
    public void setMessageToSend(String v) { messageToSend.set(v); }
    public String getGreeting() { return "Welcome to ChatApp"; }
}