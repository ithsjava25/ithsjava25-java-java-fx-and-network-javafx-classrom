package com.example;


import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model = new HelloModel();

    @FXML
    public TextField messageInput;
    @FXML
    public Button sendButton;
    @FXML
    public Button attachButton;
    @FXML
    public TextField nameInput;



    @FXML
    public ListView<HelloModel.ChatEntry> chatListView;

    @FXML
    private void initialize() {

            ObservableList<HelloModel.ChatEntry> history = model.getChatHistory();

            chatListView.setItems(history);

    }

    @FXML
    public void handleSendButton() {
        String messageText= messageInput.getText();
        String senderName=nameInput.getText();


    }
}
