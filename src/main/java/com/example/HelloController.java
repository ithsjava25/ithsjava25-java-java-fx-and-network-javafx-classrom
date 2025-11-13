package com.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import javafx.scene.layout.VBox;
import javafx.geometry.Insets;
import javafx.scene.control.Label;

import java.awt.*;
import java.net.URI;
import java.time.Instant;
import java.time.ZoneId;


import java.io.File;
import java.nio.file.Path;

import static com.example.HelloModel.runOnFx;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    @FXML
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        System.out.println("Controller init: kopplar ListView");

        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }

        messageView.setItems(model.getMessages());

        messageView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    VBox container = new VBox();
                    container.setSpacing(4);

                    Label topicLabel = new Label(item.topic());
                    topicLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #2a9df4;");

                    Label messageLabel = new Label(item.message());
                    messageLabel.setWrapText(true);
                    messageLabel.setStyle("-fx-text-fill: #333333;");

                    Label timeLabel = new Label("⏰ " + Instant.ofEpochMilli(item.time()).atZone(ZoneId.systemDefault()).toLocalDateTime());
                    timeLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #888888;");

                    container.getChildren().addAll(topicLabel, messageLabel, timeLabel);

                    // Lägg till nedladdningslänk om fil finns
                    if (item.attachmentUrl() != null && !item.attachmentUrl().isEmpty()) {
                        Hyperlink downloadLink = new Hyperlink("📎 Ladda ner fil");
                        downloadLink.setOnAction(e -> {
                            try {
                                Desktop.getDesktop().browse(new URI(item.attachmentUrl()));
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        });
                        container.getChildren().add(downloadLink);
                    }

                    container.setPadding(new Insets(8));
                    container.setStyle("-fx-background-color: #f4f4f4; -fx-background-radius: 6;");

                    setGraphic(container);
                }
            }
        });
    }

    @FXML
    private javafx.scene.control.TextField messageInput;


    public void sendMessage(ActionEvent actionEvent) {
        String content = messageInput.getText();
        model.setMessageToSend(content);
        model.sendMessage();
    }

    @FXML
    private void sendFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en fil att skicka");
        File selectedFile = fileChooser.showOpenDialog(messageInput.getScene().getWindow());

        if (selectedFile != null) {
            Path filePath = selectedFile.toPath();
            model.sendFile(filePath).thenAccept(success -> {
                runOnFx(() -> {
                    if (success) {
                        System.out.println("Fil skickad: " + filePath.getFileName());
                    } else {
                        System.out.println("Misslyckades att skicka filen.");
                    }
                });
            });
        } else {
            System.out.println("Ingen fil vald.");
        }
    }

}

