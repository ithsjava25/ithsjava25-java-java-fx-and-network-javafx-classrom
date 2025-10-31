package com.example;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();

    @FXML
    private TextField messageField;

    @FXML
    private VBox chatBox;

    @FXML
    private ScrollPane chatScroll;

    @FXML
    private void handleSend() {
        String message = messageField.getText().trim();
        if (message.isEmpty()) {
            return;
        }

        addMessageBubble("Du: " + message, true);

        String response = model.getGreeting();
        addMessageBubble(response, false);

        messageField.clear();
    }

    private void addMessageBubble(String text, boolean isUser) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(400);
        bubble.getStyleClass().add(isUser ? "user-bubble" : "bot-bubble");

        HBox container = new HBox(bubble);
        container.setPadding(new Insets(5));
        container.setAlignment(isUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        chatBox.getChildren().add(container);
    }


    @FXML
    private void initialize() {
        if (messageField != null) {
            messageField.setOnAction(e -> handleSend());
        }
        chatBox.heightProperty()
            .addListener((observable, oldValue, newValue) ->
            chatScroll.setVvalue(1.0));
    }


}
