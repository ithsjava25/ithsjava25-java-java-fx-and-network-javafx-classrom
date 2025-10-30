package com.example;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    //En model skapas som i bakgrunden är en lista och håller koll på meddelanden
    private final HelloModel model = new HelloModel();
    //@FXML kopplingar
    //Kopplar ett textfält från FXML där meddelandet skrivs
    @FXML
    private TextField messageInput;

    //Kopplar en knapp från FXML som klickas på för att skickas
    @FXML
    private Button sendButton;

    //Kopplar en låda från FXML där alla meddelanden visas
    @FXML
    private VBox chatBox;

    //Metoden körs automatiskt när appen startar
    @FXML
    private void initialize() {
        //Om användaren trycker på Enter eller klickar med musen -> skicka meddelandet
        messageInput.setOnAction((event) -> sendMessage());
        sendButton.setOnAction(event -> sendMessage());
        //Listan i model som samlar meddelanden
        model.messagesProperty()
                //Lyssna på förändringar, observable = listan, oldList = listan innan förändringar, newList = listan efter förändringar
                .addListener((observable, oldList, newList) -> {
                    //Tar bort den gamla listan för att lägga till den nya
                    chatBox.getChildren().clear();
                    //lägg till varje meddelande som en pratbubbla
                    for(String msg : newList) {
                        //Skapar en etikett med stilen från css-klassen message-bubble
                        Label label = new Label(msg);
                        label.getStyleClass().add("message-bubble");
                        //LÄgger till den i vbox
                        chatBox.getChildren().add(label);
                    }
                });

    }

    private void sendMessage() {
        //Lägger till meddelandet i listan från model och tömmer sedan fältet där text matas in(prompt-meddelande visas igen)
            model.addMessage(messageInput.getText());
            messageInput.clear();

    }
    }