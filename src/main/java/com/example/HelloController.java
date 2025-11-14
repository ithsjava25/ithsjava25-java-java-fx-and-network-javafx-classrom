package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

    @FXML
    private ListView<NtfyMessageDto> messageView;

    @FXML
    private Label messageLabel;

    @FXML
    private TextField messageInput;

    @FXML
    private void initialize() {
        messageLabel.setText(model.getGreeting());
        messageView.setItems(model.getMessages());

        // Bind textfältet till modellen
        messageInput.textProperty().bindBidirectional(model.messageToSendProperty());

        // Skicka meddelande när användaren trycker Enter
        messageInput.setOnAction(event -> {
            model.sendMessage();
            messageInput.clear(); // töm fältet efter skick
        });
    }
    @FXML
    private void attachFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select file to send");
        File file = fileChooser.showOpenDialog(messageView.getScene().getWindow());
        if (file != null) {
            model.sendFile(file.toPath());
        }
    }

}
