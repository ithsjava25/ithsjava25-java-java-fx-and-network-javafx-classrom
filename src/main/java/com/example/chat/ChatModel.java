package com.example.chat;

import javafx.application.Platform;
import javafx.scene.control.ListView;

public class ChatModel {

    private final ListView<String> messageList;

    public ChatModel(ListView<String> messageList) {
        this.messageList = messageList;
    }

    public void sendMessage(String text) {
        if (text == null || text.isBlank()) return;

        // For now: only add to UI (network comes in later commits)
        Platform.runLater(() -> messageList.getItems().add("You: " + text));
    }
}

