package com.example;


import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;

public class HelloController {

    public final HelloModel model = new HelloModel(new NtfyConnectionImpl());
    @FXML private ListView<String> messageList;
    @FXML private TextField inputField;
    @FXML private Label fileLabel;

    private File selectedFile;


    @FXML
    public void initialize() {
        messageList.setItems(model.getMessages());
    }


    public void sendMessage() {
        String message = inputField.getText().trim();
        if (!message.isEmpty()) {
            model.setMessageToSend(message);
            model.sendMessage();
            inputField.clear();
        }
        if (selectedFile != null) {
            try {
                sendFileToServer(selectedFile);
                fileLabel.setText("File sent successfully");
            } catch (IOException e) {
                fileLabel.setText("Error sending file");
            }
        } else {
            fileLabel.setText("No file selected");
        }
    }

    @FXML
    public void attachFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Files", "*.*"));
        selectedFile = fileChooser.showOpenDialog(new Stage());
        if (selectedFile != null) {
            fileLabel.setText("Selected: " + selectedFile.getName());
        } else {
            fileLabel.setText("No file selected");
        }
    }


    private void sendFileToServer(File file) throws IOException {
        model.sendFileToServer(file);
    }


}
