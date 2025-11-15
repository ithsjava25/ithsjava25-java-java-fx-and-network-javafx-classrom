package com.example.chat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ChatModel {

    private final ObservableList<String> messages = FXCollections.observableArrayList();

    public ObservableList<String> getMessages() {
        return messages;
    }

    public void addMessage(String message) {
        if (message != null && !message.isBlank()) {
            messages.add(message);
        }
    }

}


