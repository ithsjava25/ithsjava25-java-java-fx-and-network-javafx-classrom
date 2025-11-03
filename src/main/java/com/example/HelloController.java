package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 * Handles updates of the chat-window.
 */
public class HelloController {

    //En model skapas som i bakgrunden är en lista och håller koll på meddelanden
    private final HelloModel model = new HelloModel();

    //@FXML kopplingar
    //Kopplar ett textfält från FXML där användaren skriver ett meddelande
    @FXML
    private TextField messageInput;

    //Kopplar en knapp från FXML som klickas på för att skicka meddelandet
    @FXML
    private Button sendButton;

    //Kopplar en VBox från FXML där alla meddelanden visas
    @FXML
    private VBox chatBox;

    //Får chattvyn att skrolla ner till senaste meddelandet
    @FXML
    private ScrollPane messageToLast;

    //Metoden körs automatiskt när appen startar
    @FXML
    private void initialize() {
        //Sätter ursprungstillståndet (default) för skicka-knappen
        updateSendButtonState();

        //Lägger till en lyssnare för att uppdatera knappen vid inmatning av text
        messageInput.textProperty().addListener((observable, oldValue, newValue) -> {
            updateSendButtonState();
        });

        //Om användaren trycker på Enter eller klickar med musen -> skicka meddelandet
        messageInput.setOnAction((event) -> sendMessage());
        sendButton.setOnAction(event -> sendMessage());

        //Lyssna på förändringar i meddelandelistan, observable = listan,
        // oldList = listan innan förändringar, newList = listan efter förändringar
        model.getMessages()
                .addListener((observable, oldList, newList) -> {
                    //Tar bort den gamla listan för att lägga till den nya
                    chatBox.getChildren().clear();
                    //lägg till varje meddelande som en pratbubbla(Label)
                    for (String msg : newList) {
                        //Skapar en etikett med stilen från css-klassen message-bubble
                        Label label = new Label(msg);
                        label.getStyleClass().add("message-bubble");
                        //Lägger till den i vbox
                        chatBox.getChildren().add(label);

                    }
                    //Platform.runLater(() -> {
                       // messageToLast.setVvalue(1.0);
                    //});
                });

    }

    private void sendMessage() {
        //Kontrollerar om text-fältet är tomt
        //Lägger till meddelandet i listan från model och tömmer sedan fältet där text matas in(prompt-meddelande visas igen)
        if (!messageInput.getText().isEmpty()) {
            model.sendMessage();
            model.addMessages(messageInput.getText());
            messageInput.clear();
        }
    }

    private void updateSendButtonState() {
        // Kollar om texten, efter att ha tagit bort ledande/efterföljande mellanslag, är tom.
        boolean isTextPresent = !messageInput.getText().trim().isEmpty();

        // Sätt disable till TRUE om det INTE finns text.
        sendButton.setDisable(!isTextPresent);
    }
}