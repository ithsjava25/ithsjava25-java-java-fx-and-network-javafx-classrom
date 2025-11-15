package com.example.chat;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatController {

    @FXML
    private TextField inputField;

    @FXML
    private Button sendButton;

    @FXML
    private ListView<String> messageList;

    private ChatModel model;

    public void initialize() {
        
        model = new ChatModel();

        
        messageList.setItems(model.getMessages());

        //Sätt upp knapp och enter-tangent
        sendButton.setOnAction(e -> sendMessage());
        inputField.setOnAction(e -> sendMessage());
    }

    private void sendMessage() {
        String text = inputField.getText();
        if (text != null && !text.isBlank()) {
            model.addMessage(text);
            inputField.clear();
        }
    }
}



