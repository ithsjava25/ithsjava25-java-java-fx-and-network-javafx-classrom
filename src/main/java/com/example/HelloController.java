package com.example;

import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;

import java.io.IOException;
import java.util.UUID;

/**
 * Controller layer: mediates between the view (FXML) and the model.
 */
public class HelloController {

    private final HelloModel model;
    private final ChatNetworkClient httpClient;  // Abstraktion (Dependency Inversion)
    private final String hostName;
    private ChatNetworkClient.Subscription subscription;
    private long subscriptionStartTime;

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

    @FXML
    private void sendMessage() {
        String messageText = messageTextField.getText();
        if (!messageText.isEmpty()) {
            try {
                NtfyMessage message = createMessage(messageText);
                httpClient.send(hostName, message);
                messageTextField.clear();
            } catch (Exception e) {
                handleSendError(e);
            }
        }
    }

    private NtfyMessage createMessage(String text) {
        return new NtfyMessage(
                UUID.randomUUID().toString(),
                System.currentTimeMillis(),
                "message",
                "mytopic",
                text
        );
    }

    private void handleSendError(Exception e) {
        System.err.println("Fel vid sändning: " + e.getMessage());
    }


    @FXML
    private void subscribeToTopic() {
        unsubscribeFromCurrentTopic();      // Stäng den gamla prenumerationen
        model.getMessages().clear();       // Rensa gamla meddelanden
        startNewSubscription();           // Starta ny prenumeration
        model.setConnected(true);        // Uppdatera anslutningsstatus
    }

    private void unsubscribeFromCurrentTopic() {
        if (subscription != null) {
            try {
                subscription.close();
            } catch (IOException e) {
                System.err.println("Kunde inte stänga gammal prenumeration: " + e.getMessage());
            }
        }
    }

    private void startNewSubscription() {
        subscriptionStartTime = System.currentTimeMillis();  // Spara starttiden
        subscription = httpClient.subscribe(
                hostName,
                "mytopic",
                this::handleIncomingMessage
        );
    }

    private void handleIncomingMessage(NtfyMessage message) {
        System.out.println("Mottaget meddelande: " + message.id() + ", tid: " + message.time());
        long messageTimeSeconds = message.time();
        long currentTimeSeconds = System.currentTimeMillis() / 1000;

        // Hoppa över gamla meddelanden
        if (messageTimeSeconds < (subscriptionStartTime / 1000)) {
            System.out.println("Hoppar över (gammalt meddelande).");
            return;
        }

        // Hoppa över dubbletter
        if (isDuplicateMessage(message)) {
            System.out.println("Hoppar över (dublett).");
            return;
        }

        // Lägg till meddelandet i modellen
        System.out.println("Lägger till i modellen: " + message.message());
        model.addMessage(message);
    }

    private boolean isDuplicateMessage(NtfyMessage message) {
        return model.getMessages().stream()
                .anyMatch(existingMessage -> existingMessage.id().equals(message.id()));
    }


    @FXML
    private void unsubscribeFromTopic() {
        if (subscription != null) {
            closeSubscription();
            model.setConnected(false);
            subscription = null;  // Rensa referensen
        }
    }

    private void closeSubscription() {
        try {
            subscription.close();
        } catch (IOException e) {
            System.err.println("Fel vid avprenumeration: " + e.getMessage());
        }
    }
}
