package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;

import java.io.IOException;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();
    public ListView<NtfyMessageDto> messageView;

    @FXML
    private TextArea myTextArea;

    @FXML
    private TextArea chatArea;

    @FXML
    private Button chatButton;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }
        // messageView.setItems(model.sendMessage());


        chatButton.setOnAction(event -> {
            String input = chatArea.getText().trim();
            if (!input.isEmpty()) {
                // Lägg till texten i myTextArea
                myTextArea.appendText(input + "\n");
                chatArea.clear(); // Töm inmatningsfältet
            }
        });
    }

    public void sendMessage(ActionEvent actionEvent) throws IOException, InterruptedException {
        model.sendMessage();
    }

}

