package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
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

    public void setPrimaryStage(Stage stage) { this.primaryStage = stage; }

    @FXML
    private void initialize() {
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

                HBox hbox = new HBox(5);

                Label time = new Label("[" + item.getFormattedTime() + "] ");
                Label text = new Label(item.hasAttachment() ? item.getAttachmentName() : item.message());

                ImageView iconView = new ImageView();
                iconView.setFitWidth(24);
                iconView.setFitHeight(24);

                if (item.hasAttachment()) {
                    String type = item.getAttachmentContentType();

                    if (type != null && type.startsWith("image/")) {
                        Image img = model.loadImageFromUrl(item, 300, 300);
                        if (img != null) {
                            ImageView imgView = new ImageView(img);
                            imgView.setPreserveRatio(true);
                            imgView.setFitWidth(300);
                            imgView.setOnMouseClicked(e -> model.saveAttachment(item)); // Klickbar bild
                            hbox.getChildren().add(imgView);
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
                } else {
                    iconView.setImage(new Image(getClass().getResourceAsStream("/icons/messages.png")));
                }
                hbox.getChildren().addAll(iconView, time, text);
                setGraphic(hbox);
            }
            });
    }

    @FXML
    private void onSend() { sendMessage(); }

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
        if (selectedFile != null) messageLabel.setText("Vald fil: " + selectedFile.getName());
    }
}
