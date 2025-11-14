package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.net.URL;

public class HelloController {

    @FXML private ListView<NtfyMessageDto> messageView;
    @FXML private TextField inputField;
    @FXML private Label messageLabel;

    private final HelloModel model = new HelloModel();
    private Stage primaryStage;
    private File selectedFile;
    private final String myTopic = "MY_TOPIC";

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void initialize() {
        messageView.setStyle("-fx-background-color: transparent; -fx-control-inner-background: transparent;");
        messageView.setItems(model.getMessages());

        messageView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                boolean isIncoming = !myTopic.equals(item.topic());
                HBox cellBox = new HBox(5);
                cellBox.setMaxWidth(Double.MAX_VALUE);
                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
                Label text = new Label(item.hasAttachment() ? item.getAttachmentName() : item.message());
                text.setWrapText(true);
                text.setMaxWidth(400);
                text.setStyle(
                        "-fx-background-color: " + (isIncoming ? "#DDDDDDCC" : "#4CAF50CC") + ";" +
                                "-fx-text-fill: " + (isIncoming ? "black" : "white") + ";" +
                                "-fx-padding: 8;" +
                                "-fx-background-radius: 10;"
                );
                ImageView iconView = null;
                if (item.hasAttachment()) {
                    iconView = createIconForAttachment(item);
                }
                if (isIncoming) {
                    if (iconView != null) cellBox.getChildren().add(iconView);
                    cellBox.getChildren().add(text);
                } else {
                    cellBox.getChildren().add(spacer);
                    cellBox.getChildren().add(text);
                    if (iconView != null) cellBox.getChildren().add(iconView);
                }
                HBox wrapper = new HBox(cellBox);
                wrapper.setMaxWidth(Double.MAX_VALUE);
                wrapper.setAlignment(isIncoming ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
                setGraphic(wrapper);
            }
        });
    }

    @FXML private void onSend() { sendMessage(); }

    @FXML
    private void sendMessage() {
        try {
            if (selectedFile != null) {
                boolean ok = model.sendFile(selectedFile);
                messageLabel.setText(ok ? "Filen skickad" : "Fel vid fil");
                selectedFile = null;
                return;
            }
            String text = inputField.getText().trim();
            if (!text.isEmpty()) {
                model.sendMessage(text);
                inputField.clear();
                messageLabel.setText("Meddelande skickat");
            }
        } catch (Exception e) {
            messageLabel.setText("Fel: " + e.getMessage());
        }
    }

    private ImageView createIconForAttachment(NtfyMessageDto item) {
        String type = item.getAttachmentContentType();
        ImageView iconView = new ImageView();
        iconView.setFitWidth(24);
        iconView.setFitHeight(24);
        try {
            if (type != null && type.startsWith("image/")) {
                // Ladda själva bilden för chatten
                Image img = model.loadImageFromUrl(item, 100, 100);
                if (img != null) {
                    iconView.setImage(img);
                    iconView.setPreserveRatio(true);
                    iconView.setFitWidth(100);
                    iconView.setFitHeight(100);
                } else {
                    return null;
                }
            } else if ("application/pdf".equals(type)) {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/pdf.png")));
            } else if ("application/zip".equals(type)) {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/zip.png")));
            } else {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/file.png")));
            }
        } catch (Exception e) {
            messageLabel.setText("Fel vid ikonskapande: " + e.getMessage());
            if (!type.startsWith("image/")) {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/file.png")));
            } else {
                return null;
            }
        }
        return iconView;
    }

    @FXML
    private void attachFile() {
        FileChooser chooser = new FileChooser();
        selectedFile = chooser.showOpenDialog(primaryStage);
        if (selectedFile != null) messageLabel.setText("Vald fil: " + selectedFile.getName());
    }
}