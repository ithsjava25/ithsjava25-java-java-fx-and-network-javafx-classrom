package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.util.Optional;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField messageInput;

    private Stage primaryStage;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        messageLabel.setText(model.getGreeting());
        messageView.setItems(model.getMessages());
        messageInput.textProperty().bindBidirectional(model.messageToSendProperty());

        // Lägg till cell factory för att visa filinformation
        messageView.setCellFactory(lv -> new NtfyMessageListCell());
    }

    @FXML
    private void sendMessage(ActionEvent actionEvent) {
        try {
            model.sendMessage();
            messageInput.clear();
            messageLabel.setText("✅ Meddelande skickat!");
        } catch (Exception e) {
            messageLabel.setText("❌ Fel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void sendFile() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Välj fil att skicka");
            File file = fileChooser.showOpenDialog(primaryStage);

            if (file != null) {
                boolean success = model.sendFile(file);
                if (success) {
                    messageLabel.setText("✅ Fil skickad: " + file.getName());
                } else {
                    messageLabel.setText("❌ Kunde inte skicka fil");
                }
            }
        } catch (Exception e) {
            messageLabel.setText("❌ Fel: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void sendFileFromUrl() {
        try {
            // Create TextInputDialog for URL entry
            TextInputDialog urlDialog = new TextInputDialog("https://example.com/file.jpg");
            urlDialog.setTitle("Send File from URL");
            urlDialog.setHeaderText("Enter file URL");
            urlDialog.setContentText("URL:");

            // Use showAndWait() to get the result
            Optional<String> result = urlDialog.showAndWait();

            if (result.isPresent() && !result.get().isBlank()) {
                String url = result.get();
                boolean success = model.sendFileFromUrl(url);
                if (success) {
                    messageLabel.setText("✅ File from URL sent");
                } else {
                    messageLabel.setText("❌ Could not send file from URL");
                }
            }
        } catch (Exception e) {
            messageLabel.setText("❌ Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}