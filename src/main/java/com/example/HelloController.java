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

    @FXML
    private ListView<NtfyMessageDto> messageView;
    @FXML
    private TextField inputField;
    @FXML
    private Label messageLabel;

    private final HelloModel model = new HelloModel();
    private Stage primaryStage;
    private File selectedFile;
    private final String myTopic = "MY_TOPIC";

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void initialize() {
        // Transparent ListView
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

    private ImageView createIconForAttachment(NtfyMessageDto item) {
        ImageView iconView = new ImageView();
        iconView.setFitWidth(24);
        iconView.setFitHeight(24);
        String type = item.getAttachmentContentType();

        try {
            if (type != null && type.startsWith("image/")) {
                Image img = model.loadImageFromUrl(item, 100, 100);
                if (img != null) {
                    ImageView imgView = new ImageView(img);
                    imgView.setPreserveRatio(true);
                    imgView.setFitWidth(100);
                    imgView.setOnMouseClicked(e -> saveAttachment(item, primaryStage));
                    return imgView;
                }
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/image.png")));
                iconView.setOnMouseClicked(e -> saveAttachment(item, primaryStage));
            } else if ("application/pdf".equals(type)) {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/pdf.png")));
                iconView.setOnMouseClicked(e -> saveAttachment(item, primaryStage));
            } else if ("application/zip".equals(type)) {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/zip.png")));
                iconView.setOnMouseClicked(e -> saveAttachment(item, primaryStage));
            } else {
                iconView.setImage(new Image(getClass().getResourceAsStream("/icons/file.png")));
                iconView.setOnMouseClicked(e -> saveAttachment(item, primaryStage));
            }
        } catch (Exception e) {
            messageLabel.setText("Fel vid ikonskapande: " + e.getMessage());
        }

        return iconView;
    }

    @FXML
    private void onSend() {
        sendMessage();
    }

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

    @FXML
    private void attachFile() {
        FileChooser chooser = new FileChooser();
        selectedFile = chooser.showOpenDialog(primaryStage);
        if (selectedFile != null) messageLabel.setText("Vald fil: " + selectedFile.getName());
    }

    public void saveAttachment(NtfyMessageDto item, Stage stage) {
        if (item == null || !item.hasAttachment()) return;

        if (item.getAttachmentUrl() == null) {
            messageLabel.setText("Ingen URL för filen");
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setInitialFileName(item.getAttachmentName());
        File dest = chooser.showSaveDialog(stage);
        if (dest == null) return;

        new Thread(() -> {
            try (InputStream in = new URL(item.getAttachmentUrl()).openStream();
                 OutputStream out = new FileOutputStream(dest)) {

                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }

                Platform.runLater(() -> messageLabel.setText("Filen sparad: " + dest.getAbsolutePath()));
            } catch (IOException e) {
                Platform.runLater(() -> messageLabel.setText("Fel vid sparning: " + e.getMessage()));
            }
        }).start();
    }
}
