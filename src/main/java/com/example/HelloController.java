package com.example;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.io.IOException;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model;
    private final ChatNetworkClient httpClient;  // Abstraktion (Dependency Inversion)
    private final String hostName;
    private ChatNetworkClient.Subscription subscription;

    @FXML private Label messageLabel;

    //Nya FXML-komponenter:
    @FXML private ListView<NtfyMessage> messageListView;     //Visar meddelanden från HelloModel
    @FXML private TextField messageTextField;               //Används för att skriva nya meddelanden
    @FXML private Button sendButton;                       //Används för att skicka meddelanden
    @FXML private Label connectionStatusLabel;            // Status för anslutning

    //Konstruktor för Dependency Injection
    public HelloController(HelloModel model, ChatNetworkClient httpClient, String hostName) {
        this.model = model;
        this.httpClient = httpClient;
        this.hostName = hostName;
    }

    @FXML
    private void initialize() {
        if (messageLabel != null) {
            messageLabel.setText(model.getGreeting());
        }
        //Binder listView till meddelandelistan i modellen
        messageListView.setItems(model.getMessages());

        //Binder anslutningsstatus till etiketten
        connectionStatusLabel.textProperty().bind(
                Bindings.when(model.connectedProperty())
                        .then("Ansluten: ja")
                        .otherwise("Ansluten: nej")
        );
    }

    //Ny metod för att hantera knapptryckningar
    @FXML
    private void onSendButtonClick() {
        String messageText = messageTextField.getText();
        if (!messageText.isEmpty()) {
            //Skapa ett nytt meddelande och lägg till det i modellen
            NtfyMessage message = new NtfyMessage(
                    "1",  // ID (kan genereras bättre) //TODO
                    System.currentTimeMillis(), // Tid
                    "message", // Eventtyp
                    "chat", // Topic
                    messageText //Meddelandetext
            );
            model.addMessage(message);

            //Lägg till meddelandet i modellen
            model.addMessage(message);
            try {
                //Använd hostName-fältet (skickat via konstruktorn)
                httpClient.send(hostName, message);
                messageTextField.clear();
            } catch (Exception e) {
                System.err.println("Fel vid sändning: " + e.getMessage());
            }
        }
    }

    //Metod för att prenumerera på meddelanden
    @FXML
    private void onSubscribeButtonClick() {
        subscription = httpClient.subscribe(
                hostName,
                "chat",
                message -> model.addMessage(message)  //Lägg till meddelande i modellen
        );
        model.setConnected(true);
    }

    //Metod för att avbryta prenumeration
    @FXML
    private void onUnsubscribeButtonClick() {
        if (subscription != null) {
            try {
                subscription.close();
                model.setConnected(false);
            } catch (IOException e) {
                System.err.println("Fel vid avprenumeration: " + e.getMessage());
            }
        }
    }
}
