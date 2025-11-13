package com.example;


import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatModel {
    private final ObservableList<ChatMessage> messages = FXCollections.observableArrayList();

    private final NtfyConnection ntfyConnection;

    public ChatModel(NtfyConnection ntfyConnection) {
        this.ntfyConnection = ntfyConnection;
    }
    public ObservableList<ChatMessage> getMessages() {
        return messages;
    }
    public void sendMessage(String text) {
        ntfyConnection.send(text);
    }
    public void startReceiving() {

        ntfyConnection.receive(ntfyDto -> {


            ChatMessage chatMsg = new ChatMessage(ntfyDto.message(), ntfyDto.time());


            Platform.runLater(() -> {
                messages.add(chatMsg);
            });
        });
    }
}



