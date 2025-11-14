package com.example;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.awt.event.ActionEvent;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(); //new NtfyConnectionImpl()
    @FXML private ListView<String> messageList;
    @FXML private TextField inputField;


    //public ListView<String> messageListView;



    private void initialize() {
        messageList.setItems(model.getMessages());
    }

    public void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {
            model.addMessage("Test: " + message);
            inputField.clear();
        }
    }
}
