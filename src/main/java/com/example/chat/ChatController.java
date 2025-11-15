package com.example.chat;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatController {

    @FXML
    private TextField messageInput;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> messageList;

    private ChatModel model;

    public void initialize() {
        model = new ChatModel(messageList);
    }

    @FXML
    private void onSendClicked() {
        String text = messageInput.getText();
        model.sendMessage(text);
        messageInput.clear();
    }
}

