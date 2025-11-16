package com.example;

import com.example.client.ChatNetworkClient;
import com.example.domain.ChatModel;
import com.example.domain.NtfyMessage;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {
    private ChatModel model;
    private ChatNetworkClient client;
    private String baseUrl;
    private String topic;

    @FXML
    private Label messageLabel;

    @FXML
    private ListView<NtfyMessage> messagesList;

    @FXML
    private TextField messageInput;


    public void setClient(ChatNetworkClient client, String baseUrl, String topic) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.topic = topic;
    }

    public void setModel(ChatModel model) {
        this.model = model;
        messagesList.setItems(model.getMessages());
    }

    @FXML
    private void onSend() {
        String txt = messageInput.getText();
        if (txt == null || txt.isBlank()) return;

        NtfyMessage msg = new NtfyMessage(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                "message",
                topic,
                txt
        );

        try {
            client.send(baseUrl, msg);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        messageInput.clear();
    }

}

