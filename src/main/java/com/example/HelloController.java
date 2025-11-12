package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();
    public ListView<String> messageView;

    @FXML
    private Label topic;

    @FXML
    TextField input;

    @FXML
    private void initialize() {
        topic.textProperty().bind(model.topicProperty());
        messageView.setItems(model.getFormatedMessages());
        model.receiveMessage();
    }

    public void sendMessage(ActionEvent actionEvent) {
        model.sendMessage(input.getText().trim());
        input.clear();
    }

    public void changeTopic(ActionEvent actionEvent) {
        model.changeTopic();
    }
}
