package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel(null);
    public ListView<Object> messageView;

    @FXML
    private Label topic;

    @FXML
    TextField input;

    File attachment = null;

    @FXML
    private void initialize() {
        topic.textProperty().bind(model.topicProperty());
        messageView.setItems(model.getFormatedMessages());
        model.receiveMessage();
    }

    /**
     * Send a message, file if one has been attached else a string from the input field
     * @param actionEvent
     */
    public void sendMessage(ActionEvent actionEvent) {
        if(attachment != null) {
            model.sendFile(attachment);
            attachment = null;
        }
        else{
            model.sendMessage(input.getText().trim());
        }
        input.clear();
    }

    /**
     * Opens a filechooser and adds selected file as attachement to be sent
     * @param actionEvent
     */
    public void sendFile(ActionEvent actionEvent){
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Attach a file");

        Stage stage = (Stage) topic.getScene().getWindow();


        attachment = fileChooser.showOpenDialog(stage);
        if(attachment != null) {
            input.setText(attachment.getAbsolutePath());
        }
    }

    public void changeTopic(ActionEvent actionEvent) {
        model.changeTopic();
    }
}
