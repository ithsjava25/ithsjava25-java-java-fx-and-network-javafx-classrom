package com.example;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class HelloController {

    @FXML private ListView<NtfyMessageDto> messageView;
    @FXML private TextField inputField;
    @FXML private Label messageLabel;

    private HelloModel model = new HelloModel();
    private Stage primaryStage;
    private File selectedFile;
    private String myTopic = "MY_TOPIC"; // ändra till ditt topic

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    @FXML
    private void initialize() {
        // Gör ListView transparent så MatrixRain syns bakom
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

                boolean isIncoming = !item.topic().equals(myTopic);

                // Huvud HBox som fyller cellen
                HBox cellBox = new HBox(5);
                cellBox.setMaxWidth(Double.MAX_VALUE);

                // Spacer för egna meddelanden
                Region spacer = new Region();
                HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

                // Textbubbla
                Label text = new Label(item.hasAttachment() ? item.getAttachmentName() : item.message());
                text.setWrapText(true);
                text.setMaxWidth(400);
                text.setStyle(
                        "-fx-background-color: " + (isIncoming ? "#DDDDDDCC" : "#4CAF50CC") + ";" +
                                "-fx-text-fill: " + (isIncoming ? "black" : "white") + ";" +
                                "-fx-padding: 8;" +
                                "-fx-background-radius: 10;"
                );

                // Ikon för filer/bilder
                ImageView iconView = null;
                if (item.hasAttachment()) {
                    iconView = new ImageView();
                    iconView.setFitWidth(24);
                    iconView.setFitHeight(24);
                    String type = item.getAttachmentContentType();

                    if (type != null && type.startsWith("image/")) {
                        Image img = model.loadImageFromUrl(item, 100, 100);
                        if (img != null) {
                            ImageView imgView = new ImageView(img);
                            imgView.setPreserveRatio(true);
                            imgView.setFitWidth(100);
                            imgView.setOnMouseClicked(e -> model.saveAttachment(item));
                            if (isIncoming) cellBox.getChildren().add(imgView);
                            else cellBox.getChildren().add(0, imgView);
                        }
                        iconView.setImage(new Image(getClass().getResourceAsStream("/icons/image.png")));
                    } else if ("application/pdf".equals(type)) {
                        iconView.setImage(new Image(getClass().getResourceAsStream("/icons/pdf.png")));
                        iconView.setOnMouseClicked(e -> model.saveAttachment(item));
                    } else if ("application/zip".equals(type)) {
                        iconView.setImage(new Image(getClass().getResourceAsStream("/icons/zip.png")));
                        iconView.setOnMouseClicked(e -> model.saveAttachment(item));
                    } else {
                        iconView.setImage(new Image(getClass().getResourceAsStream("/icons/file.png")));
                        iconView.setOnMouseClicked(e -> model.saveAttachment(item));
                    }
                }

                // Lägg till komponenter i rätt ordning
                if (isIncoming) {
                    if (iconView != null) cellBox.getChildren().add(iconView);
                    cellBox.getChildren().add(text);
                } else {
                    cellBox.getChildren().add(spacer); // tryck text + ikon åt höger
                    cellBox.getChildren().add(text);
                    if (iconView != null) cellBox.getChildren().add(iconView);
                }

                // Wrapper HBox för alignment
                HBox wrapper = new HBox(cellBox);
                wrapper.setMaxWidth(Double.MAX_VALUE);
                wrapper.setAlignment(isIncoming ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);

                setGraphic(wrapper);
            }
        });
    }

    @FXML
    private void onSend() {
        sendMessage();
    }

    @FXML
    private void sendMessage() {
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
    }

    @FXML
    private void attachFile() {
        FileChooser chooser = new FileChooser();
        selectedFile = chooser.showOpenDialog(primaryStage);
        if (selectedFile != null)
            messageLabel.setText("Vald fil: " + selectedFile.getName());
    }
}
