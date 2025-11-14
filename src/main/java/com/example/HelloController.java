package com.example;


import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.awt.event.ActionEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoField;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    @FXML private ListView<NtfyMessageDto> messageList;
    @FXML private TextField inputField;




    private void initialize() {
        messageList.setItems(model.getMessages());
    }

    public void sendMessage() {
        String message = inputField.getText();
        if (!message.isEmpty()) {

            LocalDateTime ldt = LocalDateTime.now();
            ZonedDateTime zdt = ZonedDateTime.of(ldt, ZoneId.systemDefault());
            long date = zdt.toInstant().toEpochMilli();

            model.addMessage(new NtfyMessageDto("test id", date, "Sent Message", "test topic", "test message"));
            inputField.clear();
        }
    }
}
