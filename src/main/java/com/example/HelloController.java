package com.example;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class HelloController {


    @FXML
    private Button logoutButton;
    @FXML
    private AnchorPane scenePane;
    @FXML
    private ListView<String> chatList;
    @FXML
    private TextField messageField;
    @FXML
    private TextField usernameField;



    private NtfyClient ntfy;
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");



    @FXML
    public void initialize() {
        // Set your topic name (you can make this configurable)
        String topic = "myfxchat";
        ntfy = new NtfyClient(topic);

        // Start listening to the topic
        ntfy.subscribe(message -> Platform.runLater(() -> chatList.getItems().add(message)));
    }

    @FXML
    private void handleSend() {
        String username = usernameField.getText().isBlank() ? "Anonymous" : usernameField.getText();
        String message = messageField.getText().trim();
        if (message.isEmpty()) return;

        String time = LocalTime.now().format(timeFormat);
        String formatted = String.format("[%s] %s: %s", time, username, message);
        chatList.getItems().add(formatted);

        ntfy.sendMessage(username, message);
        messageField.clear();
    }
    @FXML
    private void handleAttachFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select a file to send");

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            System.out.println("Selected file: " + selectedFile.getAbsolutePath());
            sendFileToBackend(selectedFile);
        }
    }

    private void sendFileToBackend(File selectedFile) {
    }

    Stage stage;
    public void logout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Log out");
        alert.setHeaderText("Log out");
        alert.setContentText("Are you sure you want to logout?");

        if(alert.showAndWait().get() == ButtonType.OK){
        stage = (Stage) scenePane.getScene().getWindow();
        System.out.println("You have been logged out");
        stage.close();
        }

    }
}






