package com.example;

import com.example.domain.ChatModel;
import com.example.domain.NtfyMessage;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.util.stream.Collectors;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {
    private ChatModel model;

    public void setModel(ChatModel model) {
        this.model = model;
        model.getMessages().addListener((ListChangeListener<NtfyMessage>) c -> {
            String events = model.getMessages()
                    .stream()
                    .map(NtfyMessage::event)
                    .collect(Collectors.joining(";"));
            messageLabel.setText(events);
        });
    }

    @FXML
    private Label messageLabel;
}

