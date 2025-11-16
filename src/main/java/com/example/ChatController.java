package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.application.Platform;

public class ChatController {

    @FXML
    private ListView<String> messagesList;

    @FXML
    private TextField inputField;

    private final ChatModel model = new ChatModel();


    @FXML
    private void onSend() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            messagesList.getItems().add("Me: " + message);
            model.sendMessage(message);
            inputField.clear();
        }
    }


    @FXML
    private void initialize(){
        model.subscribe(msg -> {
            Platform.runLater(() -> messagesList.getItems().add("Friend: " + msg));
        });
    }






}
