package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;

import java.io.File;

public class HelloController {

    @FXML
    private TextArea chatArea;

    @FXML
    private TextField inputField;

    @FXML
    private Button sendButton;

    @FXML
    private Button attachButton;

    private HelloModel model;

    @FXML
    public void initialize() {
        // Läser BACKEND_URL och TOPIC från .env via HelloModel
        model = new HelloModel();

        // Lyssna på inkommande meddelanden
        model.listen(msg -> {
            Platform.runLater(() -> chatArea.appendText(msg + "\n"));
            System.out.println("📩 Mottaget: " + msg);
        });
    }

    @FXML
    protected void onSendButtonClick() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            model.sendMessage(message);
            inputField.clear();
        }
    }

    @FXML
    protected void onAttachFileClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Välj en fil att skicka");
        File file = fileChooser.showOpenDialog(chatArea.getScene().getWindow());
        if (file != null) {
            model.sendFile(file);
        }
    }
}
