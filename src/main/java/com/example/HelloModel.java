package com.example;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

public class HelloModel {

    private final NtfyConnection connection;
    private final ObservableList<NtfyMessageDto> messages = FXCollections.observableArrayList();
    //private final StringProperty messageToSend = new SimpleStringProperty();

    public HelloModel(NtfyConnection connection) {
        this.connection = connection;
    }

    public ObservableList<NtfyMessageDto> getMessages() {
        return messages;
    }

    /*public String getMessageToSend() {
        return messageToSend.get();
    }*/

    public void loadInitialMessagesAsync() {
        CompletableFuture
                .supplyAsync(connection::fetchHistory)
                .thenAccept(list -> Platform.runLater(() -> {
                    messages.setAll(list);
                    subscribeLive(); // start streaming after history
                }))
                .exceptionally(ex -> null);
    }

    private void subscribeLive() {
        connection.receive(m -> Platform.runLater(() -> messages.add(m)));
    }

    /*public StringProperty messageToSendProperty() {
        return messageToSend;
    }

    public void setMessageToSend(String message) {
        messageToSend.set(message);
    }*/

    public boolean sendMessage(String text) {
        return connection.send(text);
    }

    public boolean sendFile(Path path) throws FileNotFoundException {
        return connection.sendFile(path);
    }

    public void receiveMessage() {
        connection.receive(m -> Platform.runLater(() -> messages.add(m)));
    }


}
