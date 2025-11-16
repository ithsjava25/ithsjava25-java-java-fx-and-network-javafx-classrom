package com.example.domain;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;


public class ChatModel {
    private final ObservableList<NtfyMessage> messages = FXCollections.observableArrayList();

    public void addMessage(NtfyMessage msg) {
        messages.add(msg);
    }
    public ObservableList<NtfyMessage> getMessages() {
        return messages;
    }

}
