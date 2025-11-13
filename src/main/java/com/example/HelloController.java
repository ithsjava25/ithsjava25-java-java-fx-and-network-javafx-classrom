package com.example;

import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class HelloController {

    // Uppdaterad för att använda den externa Ntfy-servern: https://ntfy.fungover.org
    // Topicen "mytopic" läggs till automatiskt av modellen.
    private final HelloModel model = new HelloModel(new NtfyConnectionImpl("https://ntfy.fungover.org"));

    @FXML
    public ListView<NtfyMessageDto> chatListView;
    @FXML
    public Label topicLabel;

    @FXML
    public Label attachedFileLabel;

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

            // Bindning: Knappen är inaktiverad ENDAST om BÅDE meddelandet är tomt OCH fil inte är bifogad
            sendButton.disableProperty().bind(
                    Bindings.createBooleanBinding(() -> {
                                boolean isMessageEmpty = model.messageToSendProperty().get() == null ||
                                        model.messageToSendProperty().get().trim().isEmpty();
                                boolean isFileNotAttached = model.fileToSendProperty().get() == null;

                                return isMessageEmpty && isFileNotAttached;
                            },
                            model.messageToSendProperty(),
                            model.fileToSendProperty())
            );
        }

        if (topicLabel != null) {
            // Visar den fasta topicen
            topicLabel.textProperty().bind(
                    Bindings.concat("Fixed Topic: ", model.currentTopicProperty())
            );
        }

        // Hanterar visning av bifogad fil
        if (attachedFileLabel != null) {
            model.fileToSendProperty().addListener((obs, oldFile, newFile) -> {
                if (newFile != null) {
                    attachedFileLabel.setText("Attached file: " + newFile.getName());
                    attachedFileLabel.setStyle("-fx-font-style: italic;" +
                            " -fx-font-size: 12px;" +
                            " -fx-text-fill: #008000;");
                } else {
                    attachedFileLabel.setText("No file attached");
                    attachedFileLabel.setStyle("-fx-font-style: italic; " +
                            "-fx-font-size: 12px; " +
                            "-fx-text-fill: #333;");
                }
            });
            attachedFileLabel.setText("No file attached");
        }

        if (messageInput!=null){
            messageInput.textProperty().bindBidirectional(model.messageToSendProperty());
        }

        if(chatListView!=null){
            chatListView.setItems(model.getMessages());
            // Använd den enkla CellFactoryn
            chatListView.setCellFactory(param -> new SimpleMessageCell());
        }
    }

    /**
     * En mycket enkel ListCell som enbart visar texten utan anpassad layout (bubblor/färger)
     * men hanterar att visa "[File Uploaded]" när meddelandetexten är tom.
     */
    private static class SimpleMessageCell extends ListCell<NtfyMessageDto> {

        @Override
        protected void updateItem(NtfyMessageDto item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
                setStyle(null);
            } else {
                // Hämta meddelandet eller visa filstatus om meddelandet är tomt
                String displayMessage = item.message() != null && !item.message().trim().isEmpty()
                        ? item.message()
                        : (item.event().equals("file") ? "[File Uploaded]" : "");

                // Lägg till prefix för att visa om det är skickat lokalt
                String prefix = item.isLocal() ? "(Sent) " : "";

                setText(prefix + displayMessage);
                setGraphic(null);

                // Mycket enkel stil utan bubblor/färger. Använd standard utseende.
                setStyle(null);
            }
        }
    }

    @FXML
    protected void sendMessage() {
        if (model.fileToSendProperty().get() != null) {
            // Om en fil är bifogad, skicka filen och rensa bilagan i modellen
            model.sendFile();
        } else {
            // Annars, skicka textmeddelandet
            model.sendMessage();
        }

        if (messageInput!=null){
            messageInput.requestFocus();
        }
    }

    @FXML
    protected void attachFile() {
        // Hämta scenen från en av kontrollerna
        Stage stage = (Stage) (chatListView.getScene().getWindow());

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose file to attach");

        File selectedFile = fileChooser.showOpenDialog(stage);

        if (selectedFile != null) {
            model.setFileToSend(selectedFile);
        }
    }
}