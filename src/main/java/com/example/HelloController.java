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

    private HelloModel model = new HelloModel(new NtfyConnectionImpl());
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
                if (empty || item == null) { setText(null); setGraphic(null); return; }

                HBox hbox = new HBox(5);
                Label time = new Label("[" + item.getFormattedTime() + "] ");
                Label text = new Label(item.hasAttachment() ? item.getAttachmentName() : item.message());

                hbox.getChildren().addAll(time, text);

                // Visa bild om det är en bildbilaga
                if (item.hasAttachment() && item.getAttachmentContentType() != null &&
                        item.getAttachmentContentType().startsWith("image/")) {
                    Image img = model.loadImageFromUrl(item);
                    if (img != null) hbox.getChildren().add(new ImageView(img));
                }

                setGraphic(hbox);
            }
        });
    }

    @FXML
    private void onSend() {
        // Koppla till sendMessage-funktionen
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
        if (selectedFile != null) messageLabel.setText("Vald fil: " + selectedFile.getName());
    }
}
