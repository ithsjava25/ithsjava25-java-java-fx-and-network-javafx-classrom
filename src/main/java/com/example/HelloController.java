package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();

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
        chatButton.setOnAction(event -> {
            String input = chatArea.getText().trim();
            if (!input.isEmpty()) {
                // Lägg till texten i myTextArea
                myTextArea.appendText(input + "\n");
                chatArea.clear(); // Töm inmatningsfältet
            }
        });
    }

}

