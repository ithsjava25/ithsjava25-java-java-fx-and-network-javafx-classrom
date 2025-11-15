package com.example;


import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    public final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    @FXML public ListView<String> messageList;
    @FXML public TextField inputField;


    @FXML
    public void initialize() {
        messageList.setItems(model.getMessages());
    }


    public void sendMessage() {
        String message = inputField.getText().trim();


        if (!message.isEmpty()) {
//            LocalDateTime ldt = LocalDateTime.now();
//            ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
//            long date = zdt.toInstant().toEpochMilli();
            model.setMessageToSend(message);
            model.sendMessage(); //new NtfyMessageDto("test id", date, "Sent Message", "test topic", message)
            inputField.clear();
        }
    }
}
