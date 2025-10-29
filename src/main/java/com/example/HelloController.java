package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();
    public TextField messageInput;
    public Button sendButton;

    @FXML
    private VBox chatBox;

    @FXML
    private Label messageLabel;

    @FXML
    private void initialize() {
        if (chatBox != null) {
            Label msg1 = new Label("Hej! Hur mår du?");
            Label msg2 = new Label("Jag mår bra, tack! 🐱");

            chatBox.getChildren().addAll(msg1, msg2);
        }

    }
}
