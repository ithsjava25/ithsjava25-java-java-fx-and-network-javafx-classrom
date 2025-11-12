package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.application.Platform;

public class HelloController {

    // Kopplas till FXML-filen
    @FXML private TextField inputField;
    @FXML private Button sendBtn;
    @FXML private ListView<String> messages;

    // Skapar modellen som sköter nätverksdelen (skicka/ta emot)
    private final HelloModel model = new HelloModel();

    // Körs automatiskt när appen startar
    @FXML
    public void initialize() {
        // Starttext när programmet öppnas
        messages.getItems().add("Ansluten till: " + model.info());
        messages.getItems().add("Skriv ett meddelande och tryck Skicka!");

        // Börja lyssna efter inkommande meddelanden
        startListeningToMessages();

        // När man trycker på Skicka-knappen
        sendBtn.setOnAction(_ -> sendMessage());
    }

    // Skickar ett meddelande till chatten
    private void sendMessage() {
        String text = inputField.getText();

        if (text == null || text.isBlank()) {
            return; // Skicka inte tomt meddelande
        }

        // Visa direkt i listan
        messages.getItems().add("Jag: " + text);

        // Skicka till servern
        model.sendMessage(text);

        // Töm textfältet
        inputField.clear();
    }

    // Lyssnar efter nya meddelanden i bakgrunden
    private void startListeningToMessages() {
        model.startListening(
            // När ett nytt meddelande kommer
            msg -> Platform.runLater(() ->messages.getItems().add("Inkommande: " + msg)
            ),

            //Om ett fel inträffar
            err -> Platform.runLater(() ->messages.getItems().add("Fel: " + err.getMessage())
            )
        );
    }
}