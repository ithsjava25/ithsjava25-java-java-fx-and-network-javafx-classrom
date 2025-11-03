package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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


    //Ytan för alla meddelanden som visas
    @FXML
    private ListView<NtfyMessageDto> chatBox;

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
        messageInput.setOnAction((event) -> sendMessageToModel());
        sendButton.setOnAction(event -> sendMessageToModel());

        //Kopplar Listan i view med ObservableList i HelloModel
        chatBox.setItems(model.getMessages());

        //Styr hur varje meddelande ska visas i chatboxen
        chatBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(NtfyMessageDto item, boolean empty) {
                super.updateItem(item, empty);
                //Kräver en null check då JavaFX återanvänder cellerna
               if (item == null || empty) {
                   setText(null);
                   setGraphic(null);
            }
            else{
                //Skapar en label med meddelande-texten och sätter en stil från css
                   Label label = new Label(item.message());
                    label.getStyleClass().add("message-bubble");
                    setGraphic(label);
                }
            }
        });

    }

    private void sendMessageToModel() {
        //Kontrollerar om text-fältet är tomt
        if (!messageInput.getText().isEmpty()) {
            model.sendMessage(messageInput.getText());
            NtfyMessageDto dto = new NtfyMessageDto(String.valueOf(System.currentTimeMillis())
                    , System.currentTimeMillis()
                    ,"Message"
                    ,"topic"
                    ,messageInput.getText()
            );
            //Lägger till meddelandet i listan från model och tömmer sedan fältet där text matas in(prompt-meddelande visas igen)
            model.addMessages(dto);
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