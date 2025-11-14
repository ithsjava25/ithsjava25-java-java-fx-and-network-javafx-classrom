package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML
    private Label messageLabel;

    @FXML
    private ListView<NtfyMessageDto> messageView;

    @FXML
    private TextField inputMessage;

    @FXML
    private void initialize() {
        // Bind greeting label
        messageLabel.setText(model.getGreeting());

        // Bind ListView to model messages
        messageView.setItems(model.getMessages());
    }

    @FXML
    private void sendMessage(ActionEvent event) {
        String text = inputMessage.getText();

        // Modellens API kräver setMessageToSend() + sendMessage()
        model.setMessageToSend(text);
        model.sendMessage();

        // Rensa input-fältet (matchar även testförväntningarna)
        inputMessage.clear();
    }

    @FXML
    private void attachFile(ActionEvent event) {
        FileChooser chooser = new FileChooser();
        File file = chooser.showOpenDialog(messageView.getScene().getWindow());
        if (file != null) {
            model.sendFile(file);
        }
    }
}


