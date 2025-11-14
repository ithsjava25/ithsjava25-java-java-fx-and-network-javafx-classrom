package com.example;

import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    @FXML private TextArea inputField;
    @FXML private Button sendButton;
    @FXML private TextArea chatArea;

    private final HelloModel model = new HelloModel();


    @FXML
    private void initialize() {

        model.getMessages().addListener((ListChangeListener<NtfyMessageDto>) change ->{
            while(change.next()) {
                if (change.wasAdded()) {
                    for (var msg : change.getAddedSubList()) {
                        chatArea.appendText(msg.message() + "\n");
                    }
                }
            }
        });

        sendButton.setOnAction(e -> sendMessage());

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER && !event.isShiftDown()) {
                sendMessage();
                event.consume();
            }
        });
    }
    @FXML
    private void sendMessage() {
        String msg = inputField.getText().trim();
        if (msg.isEmpty()) return;

        chatArea.appendText("Du: " + msg + "\n");
        model.setMessageToSend(msg);
        model.sendMessage();
        inputField.clear();
    }
}
