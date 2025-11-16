package com.example;

import com.example.client.ChatNetworkClient;
import com.example.domain.ChatModel;
import com.example.domain.NtfyEventResponse;
import com.example.domain.NtfyMessage;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

public class HelloController {
    private ChatNetworkClient client;
    private String baseUrl;
    private String topic;

    @FXML
    private Label messageLabel;

    @FXML
    private ListView<NtfyEventResponse> messagesList;

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
        messagesList.setItems(model.getMessages());
        messagesList.setCellFactory(list -> new MessageCell());
    }

    private static String formatTime(long epochSeconds) {
        Instant instant = Instant.ofEpochSecond(epochSeconds);
        LocalTime time = LocalTime.ofInstant(instant, ZoneId.systemDefault());
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
                .attach(null)
                .filename(null)
                .build();

        try {
            client.send(baseUrl, msg);
            messageLabel.setText(null);
        } catch (InterruptedException | IOException e) {
            messageLabel.setText("Failed to send: " + e.getMessage());
        }

        messageInput.clear();
        titleInput.clear();
        tagsInput.clear();
    }

    private static final class MessageCell extends ListCell<NtfyEventResponse> {
        @Override
        protected void updateItem(NtfyEventResponse msg, boolean empty) {
            super.updateItem(msg, empty);

            if (empty || msg == null) {
                setText(null);
                setGraphic(null);
                return;
            }

            setText(null);

            VBox container = new VBox();
            container.setSpacing(4);
            container.setStyle("-fx-padding: 8;");

            if (msg.title() != null) {
                Label titleLabel = new Label(msg.title());
                titleLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");
                container.getChildren().add(titleLabel);
            }

            if (msg.attachment() != null) {
                NtfyEventResponse.Attachment att = msg.attachment();

                if (att.type() != null && att.type().startsWith("image")) {
                    Image image = new Image(att.url(), 300, 0, true, true);
                    ImageView imageView = new ImageView(image);
                    container.getChildren().add(imageView);
                } else {

                    // Allow user to open file URL
                    Label fileLabel = getFileLabel(att);
                    container.getChildren().add(fileLabel);
                }
            }

            if (msg.message() != null && !msg.message().isBlank()) {
                Label messageLabel = new Label(msg.message());
                messageLabel.setWrapText(true);
                messageLabel.setStyle("-fx-font-size: 14px;");
                container.getChildren().add(messageLabel);
            }

            if (msg.tags() != null && !msg.tags().isEmpty()) {
                Label tagsLabel = new Label(String.join(", ", msg.tags()));
                tagsLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #3a6ea5;");
                container.getChildren().add(tagsLabel);
            }

            if (msg.time() != null) {
                Label timeLabel = new Label(formatTime(msg.time()));
                timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: gray;");
                container.getChildren().add(timeLabel);
            }

            setGraphic(container);
        }

        private static Label getFileLabel(NtfyEventResponse.Attachment att) {
            Label fileLabel = new Label("Open file: " + (att.name() != null ? att.name() : att.url()));
            fileLabel.setStyle("-fx-text-fill: #2c75ff; -fx-underline: true;");
            fileLabel.setOnMouseClicked(ev -> {
                try {
                    String url = att.url();

                    // method that works on linux as Desktop is not always supported and crashes application
                    if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                        new ProcessBuilder("xdg-open", url).start();
                        return;
                    }

                    if (Desktop.isDesktopSupported()) {
                        Desktop.getDesktop().browse(new URI(url));
                    }

                } catch (IOException | URISyntaxException ex) {
                    throw new RuntimeException(ex);
                }
            });
            return fileLabel;
        }
    }
}
