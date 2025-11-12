package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class HelloController {
    @FXML private TextArea chatArea;
    @FXML private TextField inputField;

    private HelloModel model;

    @FXML
    public void initialize() {
        model = new HelloModel("javafx-chat"); // Du kan byta topic-namnet
        model.listen(msg -> chatArea.appendText(msg + "\n"));
    }

    @FXML
    public void onSendButtonClick() {
        String text = inputField.getText().trim();
        if (!text.isEmpty()) {
            model.sendMessage(text);
            inputField.clear();
        }
    }
}
