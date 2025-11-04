package com.example;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class HelloController {

    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    public ListView<NtfyMessageDto> chatListView;

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

        if (messageInput!=null){
            messageInput.textProperty().bindBidirectional(model.messageToSendProperty());
        }

        if(chatListView!=null){
            chatListView.setItems(model.getMessages());
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
    public void attachFile(ActionEvent actionEvent){

    }


}
