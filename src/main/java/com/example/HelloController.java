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
import java.util.List;
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
    @FXML
    private TextField titleInput;

    @FXML
    private TextField tagsInput;


    public void setClient(ChatNetworkClient client, String baseUrl, String topic) {
        this.client = client;
        this.baseUrl = baseUrl;
        this.topic = topic;
    }

    public void setModel(ChatModel model) {
        this.model = model;

        messagesList.setItems(model.getMessages());

        messagesList.setCellFactory(list -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(NtfyMessage msg, boolean empty) {
                super.updateItem(msg, empty);

                if (empty || msg == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }

                var container = new javafx.scene.layout.VBox();

                if (msg.title() != null) {
                    var titleLabel = new javafx.scene.control.Label(msg.title());
                    titleLabel.setStyle("-fx-font-size: 12px; -fx-font-weight: bold;");
                    container.getChildren().add(titleLabel);
                }

                var messageLabel = new javafx.scene.control.Label(msg.message());
                messageLabel.setStyle("-fx-font-size: 14px;");
                container.getChildren().add(messageLabel);

                if (msg.tags() != null && !msg.tags().isEmpty()) {
                    var tagsLabel = new javafx.scene.control.Label(String.join(", ", msg.tags()));
                    tagsLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: darkblue;");
                    container.getChildren().add(tagsLabel);
                }

                var timeLabel = new javafx.scene.control.Label(formatTime(msg.time()));
                timeLabel.setStyle("-fx-font-size: 9px; -fx-text-fill: gray;");
                container.getChildren().add(timeLabel);

                container.setSpacing(2);
                setGraphic(container);
            }
        });


    }

    private static String formatTime(long epochMillis) {
        var instant = java.time.Instant.ofEpochMilli(epochMillis);
        var time = java.time.LocalTime.ofInstant(instant, java.time.ZoneId.systemDefault());
        return time.toString();
    }

    @FXML
    private void onSend() {
        String txt = messageInput.getText();
        if (txt == null || txt.isBlank()) return;

        String title = titleInput.getText();
        if (title != null && title.isBlank()) title = null;

        String tagsRaw = tagsInput.getText();
        List<String> tags = null;

        if (tagsRaw != null && !tagsRaw.isBlank()) {
            tags = java.util.Arrays.stream(tagsRaw.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();
        }


        NtfyMessage msg = new NtfyMessage.Builder()
                .id(UUID.randomUUID().toString())
                .time(System.currentTimeMillis())
                .event("message")
                .topic(topic)
                .message(txt)
                .title(title)
                .tags(tags)
                .build();

        try {
            client.send(baseUrl, msg);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }

        messageInput.clear();
        titleInput.clear();
        tagsInput.clear();
    }

}

