package com.example;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;


public class ChatController {

    @FXML private ListView<ChatMessage> messageListView;
    @FXML private TextField inputTextField;
    @FXML private Button sendButton;

    private final ChatModel model;

    private final DateTimeFormatter timeFormatter =
            DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());


    public ChatController(ChatModel model) {
        this.model = model;
    }

    @FXML
    public void initialize() {
        messageListView.setItems(model.getMessages());

        messageListView.setCellFactory(lv -> new javafx.scene.control.ListCell<ChatMessage>() {
            @Override
            protected void updateItem(ChatMessage msg, boolean empty) {
                super.updateItem(msg, empty);
                if (empty || msg == null) {
                    setText(null);
                } else {
                    String formattedTime = timeFormatter.format(Instant.ofEpochSecond(msg.timestamp()));
                    setText("[" + formattedTime + "] " + msg.content());
                }
            }
        });


        inputTextField.setOnAction(event -> sendMessageAction());
        sendButton.setOnAction(event -> sendMessageAction());
    }


    private void sendMessageAction() {
        String message = inputTextField.getText().trim();
        if (!message.isEmpty()) {

            model.sendMessage(message);


            inputTextField.clear();
        }
    }


}
