package com.example;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    @FXML
    public ListView<NtfyMessageDto> chatListView;
    @FXML
    public Label topicLabel;

    @FXML
    private ComboBox topicComboBox;

    @FXML
    private Button sendButton;

    @FXML
    private TextField messageInput;

    @FXML
    private Button attachFile;

    @FXML
    private void initialize() {
        if (sendButton != null) {
            sendButton.setText(model.getGreeting());
        }

        if (topicComboBox != null) {
            topicComboBox.setItems(FXCollections.observableArrayList(Arrays.asList("general", "funStuff", "support")));

            topicComboBox.getSelectionModel().select(model.currentTopicProperty().get());

        }

        if (messageInput!=null){
            messageInput.textProperty().bindBidirectional(model.messageToSendProperty());
        }

        if(chatListView!=null){
            chatListView.setItems(model.getMessages());
        }


    }

    @FXML
    public void handleTopicChange(ActionEvent actionEvent) {
        String newTopic = topicComboBox.getSelectionModel().getSelectedItem();
        if (newTopic != null) {
            model.reconnectToTopic(newTopic);
        }
    }

    @FXML
    public void sendMessage(ActionEvent actionEvent) {
        model.sendMessage();

        model.setMessageToSend("");

        if (messageInput!=null){
            messageInput.requestFocus();
        }
    }
    @FXML
    public void attachFile(ActionEvent actionEvent) throws FileNotFoundException {

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file to attach");

        File selectedFile = fileChooser.showOpenDialog(chatListView.getScene().getWindow());

        if (selectedFile != null) {
            // 3. SKICKA FILEN TILL MODELLEN
            // Vi behöver en ny metod i HelloModel för att hantera detta.
            model.sendFile(selectedFile);
        }

    }


    public void handleTopicChange(ActionEvent actionEvent) {
    }
}
