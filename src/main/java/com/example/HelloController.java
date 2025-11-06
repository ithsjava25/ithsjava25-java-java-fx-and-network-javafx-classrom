package com.example;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 * Handles updates of the chat-window.
 */
public class HelloController {

    //En model skapas som i bakgrunden är en lista och håller koll på meddelanden
    private final HelloModel model = new HelloModel(new NtfyConnectionImpl());

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



        //model.receiveMessage();
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

                    String time = item.formattedTime();
                    Label labelTime = new Label(time);
                    labelTime.getStyleClass().add("time-stamp");

                    //Layout
                   VBox messageBox = new VBox(label, labelTime);
                   messageBox.setSpacing(2);

                   //Vänster eller höger i ListView
                   HBox hbox = new HBox(messageBox);
                   hbox.setMaxWidth(chatBox.getWidth()-20);

                   String messagePosition = item.message();
                   if (messagePosition != null && messagePosition.startsWith("User:")) {
                       hbox.setAlignment(javafx.geometry.Pos.CENTER_RIGHT);
                       label.getStyleClass().add("outgoing-message");
                   } else {
                       hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                       label.getStyleClass().add("incoming-message");
                   }
                    setGraphic(hbox);
                }
            }
        });
        //Kopplar Listan i view med ObservableList i HelloModel
        chatBox.setItems(model.getMessages());
    }

    private void sendMessageToModel() {
        String outgoingMessage = messageInput.getText().trim();
        //Kontrollerar om text-fältet är tomt
        if (!outgoingMessage.isEmpty()) {
            model.sendMessage("User: " + outgoingMessage);
            //tömmer sedan fältet där text matas in(prompt-meddelande visas igen)
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